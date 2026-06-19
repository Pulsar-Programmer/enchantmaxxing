package net.nosam08.enchantmaxxing.aom.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.nosam08.enchantmaxxing.aom.ds.OrderString;
import net.nosam08.enchantmaxxing.aom.ds.SimEnchantment;
import net.nosam08.enchantmaxxing.aom.ds.SimItem;
import net.nosam08.enchantmaxxing.aom.ds.SimReport;
import net.nosam08.enchantmaxxing.emm.EnchantmaxBuilder;
import net.nosam08.enchantmaxxing.tooltips.ds.EnchantmaxProfile;
import net.nosam08.enchantmaxxing.tooltips.ds.ItemStackKey;

public class AnvilOrdering {
    /**
     * Cache of solved orders, keyed by a content {@link #signature}. Previously keyed by
     * {@code Pair<ItemStackKey, EnchantmaxProfile>} — but {@code net.minecraft.util.Pair} has no
     * {@code equals}/{@code hashCode}, so every lookup missed and the (exponential) solve reran on
     * every menu open. A value-based string key makes it actually hit, and lets the solved order
     * be persisted and reloaded across restarts (see {@link #seed}/{@link #peek}). Concurrent
     * because the background solver thread writes to it while the client thread reads.
     */
    public static ConcurrentHashMap<String, Pair<String, Integer>> STORE = new ConcurrentHashMap<>();

    /** Deterministic, restart-stable key for an (item, profile) pair: item id + base work
     * penalty + the sorted profile. The solved cost/order depend only on these. */
    public static String signature(ItemStackKey item, EnchantmaxProfile enchantments){
        var stack = item.inner();
        var pwp_item = stack.get(DataComponentTypes.REPAIR_COST);
        int base_pwp = pwp_item != null ? pwp_item : 0;

        var entries = enchantments.profile.stream()
            .map(AnvilOrdering::serialize_enchantment)
            .sorted()
            .collect(Collectors.joining(","));

        return Registries.ITEM.getId(stack.getItem()) + "|" + base_pwp + "|" + entries;
    }

    /** Returns the already-solved (order, cost) for this task, or null if not cached yet. */
    public static Pair<String, Integer> peek(ItemStackKey item, EnchantmaxProfile enchantments){
        return STORE.get(signature(item, enchantments));
    }

    /** Pre-populates the cache with a previously-solved order (used when loading from disk). */
    public static void seed(ItemStackKey item, EnchantmaxProfile enchantments, String order, int cost){
        STORE.put(signature(item, enchantments), new Pair<>(order, cost));
    }

    /** Background solver thread so a large profile never freezes the client; results land in
     * {@link #STORE} and the menu picks them up on its next tick. Single daemon thread. */
    private static final ExecutorService SOLVER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ftt-aom-solver");
        t.setDaemon(true);
        return t;
    });
    /** Signatures currently being solved off-thread, so the same one is never queued twice. */
    private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();

    /** Returns the solved order if it is already cached, otherwise null (never computes). */
    public static OrderString cached(ItemStackKey item, EnchantmaxProfile enchantments){
        var result = STORE.get(signature(item, enchantments));
        if(result == null) return null;
        return new OrderString(item.inner(), result.getLeft(), result.getRight());
    }

    /** Whether a solve for this task is still running in the background. */
    public static boolean is_loading(ItemStackKey item, EnchantmaxProfile enchantments){
        return PENDING.contains(signature(item, enchantments));
    }

    /**
     * Non-blocking order lookup. Returns the cached order if ready; otherwise kicks off the solve on
     * the background thread and returns null. The pure-int inputs are gathered here on the client
     * thread (safe ItemStack/registry reads) so the worker never touches game state.
     */
    public static OrderString request(ItemStackKey item, EnchantmaxProfile enchantments){
        String key = signature(item, enchantments);
        var result = STORE.get(key);
        if(result != null) return new OrderString(item.inner(), result.getLeft(), result.getRight());

        if(PENDING.add(key)){
            var pwp_item = item.inner().get(DataComponentTypes.REPAIR_COST);
            int base_pwp = pwp_item != null ? pwp_item : 0;
            var e = enchantments.profile.stream()
                .map(SimEnchantment::from_enchantment)
                .collect(Collectors.toCollection(ArrayList::new));
            SOLVER.execute(() -> {
                try {
                    STORE.put(key, solve(new SimItem(base_pwp, "OBJ"), e));
                } finally {
                    PENDING.remove(key);
                }
            });
        }
        return null;
    }

    /**
     * Minimum-cost combine order via a memoized DP.
     *
     * Explores the same BASIC (apply an enchantment straight onto the item) and CHOOSE (merge two
     * enchantments into a book first) options the old {@code n_set}/{@code parse_paths} brute force
     * did, so it returns an identical optimum. The key realisation: the cost to finish a subproblem
     * depends only on the object's work penalty and the *multiset of (pwp, cost)* still in the pool,
     * never on which enchantments they are. So {@link #min_cost} memoizes that canonical state and
     * collapses the many ways to reach it (combine A then B == B then A, etc.) into one.
     *
     * The order *string* depends on identifiers, so it can't share the memo. Once the costs are
     * known, {@link #reconstruct} replays the optimal moves once on the real SimItem/SimEnchantment
     * objects (which carry the identifiers) to build the nested string. Runs off-thread; see
     * {@link #request}.
     */
    public static Pair<String, Integer> solve(SimItem o, ArrayList<SimEnchantment> e){
        if(e.isEmpty()){
            return new Pair<String, Integer>("OBJ", 0);
        }
        HashMap<String, Integer> memo = new HashMap<>();
        int cost = min_cost(o.pwp, pool_of(e), memo);
        String order = reconstruct(o, e, memo);
        return new Pair<String, Integer>(order, cost);
    }

    /** Canonical pool representation: one {pwp, cost} pair per enchantment, identities dropped. */
    private static ArrayList<int[]> pool_of(ArrayList<SimEnchantment> e){
        var pool = new ArrayList<int[]>(e.size());
        for(var x : e) pool.add(new int[]{x.pwp, x.cost});
        return pool;
    }

    /** Memo key: object work-penalty plus the sorted multiset of (pwp, cost) — order-independent. */
    private static String state_key(int o_pwp, ArrayList<int[]> pool){
        var sorted = new ArrayList<>(pool);
        sorted.sort((a, b) -> a[0] != b[0] ? a[0] - b[0] : a[1] - b[1]);
        StringBuilder sb = new StringBuilder().append(o_pwp).append('|');
        for(var p : sorted) sb.append(p[0]).append(',').append(p[1]).append(';');
        return sb.toString();
    }

    private static ArrayList<int[]> without(ArrayList<int[]> pool, int... indices){
        var copy = new ArrayList<int[]>(pool);
        var sorted = indices.clone();
        java.util.Arrays.sort(sorted);
        for(int k = sorted.length - 1; k >= 0; k--) copy.remove(sorted[k]); // high-to-low: indices stay valid
        return copy;
    }

    /** Minimum additional cost to combine the whole pool onto the object. Memoized by state. */
    private static int min_cost(int o_pwp, ArrayList<int[]> pool, HashMap<String, Integer> memo){
        if(pool.size() == 1){
            int[] x = pool.get(0);
            return o_pwp + x[0] + x[1]; // combine object with the last enchantment
        }

        String key = state_key(o_pwp, pool);
        Integer cached = memo.get(key);
        if(cached != null) return cached;

        int best = Integer.MAX_VALUE;

        // BASIC: combine each enchantment straight onto the object.
        for(int i = 0; i < pool.size(); i++){
            int[] x = pool.get(i);
            int immediate = o_pwp + x[0] + x[1];
            int total = immediate + min_cost(adv(o_pwp), without(pool, i), memo);
            if(total < best) best = total;
        }

        // CHOOSE: merge a target i with a sacrifice j into a book, then continue.
        for(int i = 0; i < pool.size(); i++){
            for(int j = 0; j < pool.size(); j++){
                if(i == j) continue;
                int[] xi = pool.get(i), xj = pool.get(j);
                int immediate = xi[0] + xj[0] + xj[1];
                var next = without(pool, i, j);
                next.add(new int[]{adv(Math.max(xi[0], xj[0])), xi[1] + xj[1]});
                int total = immediate + min_cost(o_pwp, next, memo);
                if(total < best) best = total;
            }
        }

        memo.put(key, best);
        return best;
    }

    /** Rebuilds the optimal nested order string by replaying the cost-optimal moves on real objects. */
    private static String reconstruct(SimItem o, ArrayList<SimEnchantment> e, HashMap<String, Integer> memo){
        if(e.size() == 1){
            return SimReport.combine(o.clone(), e.get(0)).operation;
        }

        int target = min_cost(o.pwp, pool_of(e), memo);

        // BASIC
        for(int i = 0; i < e.size(); i++){
            var x = e.get(i);
            int immediate = o.pwp + x.pwp + x.cost;
            var rest = new ArrayList<>(e);
            rest.remove(i);
            if(immediate + min_cost(adv(o.pwp), pool_of(rest), memo) == target){
                var no = o.clone();
                SimReport.combine(no, x); // advances the object's pwp + identifier
                return reconstruct(no, rest, memo);
            }
        }

        // CHOOSE
        for(int i = 0; i < e.size(); i++){
            for(int j = 0; j < e.size(); j++){
                if(i == j) continue;
                var xi = e.get(i);
                var xj = e.get(j);
                int immediate = xi.pwp + xj.pwp + xj.cost;
                var rest = new ArrayList<>(e);
                rest.remove(Math.max(i, j)); // remove the higher index first so the lower stays valid
                rest.remove(Math.min(i, j));
                rest.add(SimEnchantment.merged(xi, xj));
                if(immediate + min_cost(o.pwp, pool_of(rest), memo) == target){
                    return reconstruct(o.clone(), rest, memo);
                }
            }
        }

        return e.get(0).identifier; // unreachable: some move always matches the optimum
    }


    






    

    


    /** Advances the PWP of an object. */
    public static Integer adv(Integer x){
        return x * 2 + 1;
    }

    /** Creates a String from the enchantment. */
    public static String serialize_enchantment(EnchantmentLevelEntry entry){
        return entry.enchantment().getIdAsString() + ";" + entry.level();
    }

    /** Creates an ItemStack from the Enchantment. */
    public static ItemStack deserialize_enchantment(String enchantment) {
        String[] parts = enchantment.split(";");
        if (parts.length != 2) {
            return new ItemStack(Items.ENCHANTED_BOOK);
        }
        
        String enchantmentId = parts[0];
        int level = Integer.parseInt(parts[1]);
        
        Identifier id = Identifier.tryParse(enchantmentId);
        if (id == null) {
            return new ItemStack(Items.ENCHANTED_BOOK);
        }
        
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        
        RegistryWrapper<Enchantment> enchantmentRegistry = EnchantmaxBuilder.all_enchantments();
        Optional<RegistryEntry.Reference<Enchantment>> enchantmentEntry = enchantmentRegistry.getOptional(
            RegistryKey.of(RegistryKeys.ENCHANTMENT, id)
        );

        book.addEnchantment(enchantmentEntry.get(), level);
        
        return book;
    }



}
