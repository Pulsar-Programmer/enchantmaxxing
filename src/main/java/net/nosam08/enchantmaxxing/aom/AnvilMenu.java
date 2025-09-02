package net.nosam08.enchantmaxxing.aom;

import org.jetbrains.annotations.NotNull;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import net.nosam08.enchantmaxxing.tooltips.ds.EnchantmaxProfile;

public class AnvilMenu extends BaseOwoScreen<FlowLayout>  {

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        // TODO Auto-generated method stub
        //what will this look like?
    }

    /** Starts the creation of the menu. */
    public static AnvilMenu start(EnchantmaxProfile profile){
        //TODO
        return new AnvilMenu();
    }
    
}
