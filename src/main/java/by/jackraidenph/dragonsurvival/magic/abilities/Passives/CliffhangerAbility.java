package by.jackraidenph.dragonsurvival.magic.abilities.Passives;

import by.jackraidenph.dragonsurvival.config.ConfigHandler;
import by.jackraidenph.dragonsurvival.magic.common.PassiveDragonAbility;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

public class CliffhangerAbility extends PassiveDragonAbility
{
	public CliffhangerAbility(String abilityId, String icon, int minLevel, int maxLevel)
	{
		super(abilityId, icon, minLevel, maxLevel);
	}
	
	public int getHeight(){
		return getLevel();
	}
	
	@Override
	public CliffhangerAbility createInstance()
	{
		return new CliffhangerAbility(id, icon, minLevel, maxLevel);
	}
	
	@Override
	public IFormattableTextComponent getDescription()
	{
		return new TranslationTextComponent("ds.skill.description." + getId(), 3 + getHeight() + ConfigHandler.SERVER.forestFallReduction.get());
	}
	
	@OnlyIn( Dist.CLIENT )
	public ArrayList<ITextComponent> getLevelUpInfo(){
		ArrayList<ITextComponent> list = super.getLevelUpInfo();
		list.add(new TranslationTextComponent("ds.skill.range.blocks", "+1"));
		return list;
	}
}
