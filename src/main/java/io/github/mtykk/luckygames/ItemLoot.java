package io.github.mtykk.luckygames;

import org.bukkit.inventory.ItemStack;

public class ItemLoot{
    private final ItemStack _itemStack;
    private final int _minAmount;
    private final int _maxAmount;

    ItemLoot(ItemStack itemStack, int minAmount, int maxAmount) throws IllegalArgumentException{
        _itemStack = itemStack;
        if(minAmount < 0 || maxAmount < minAmount) throw  new IllegalArgumentException();
        _minAmount = minAmount;
        _maxAmount = maxAmount;
    }

    /**
     * @return Direct reference to the ItemStack
     */
    ItemStack getItemStack(){
        return _itemStack;
    }

    /**
     * @return A clone of the ItemStack, with the amount randomized according to set values
     */
    ItemStack getAmountRandomizedItemStack(){
        ItemStack res = _itemStack.clone();
        int amount = _maxAmount == _minAmount ? _maxAmount : PluginRandom.randomGenerator.nextInt(_maxAmount-_minAmount+1)+_minAmount;
        res.setAmount(amount);
        return res;
    }
}