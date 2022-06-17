/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.entities.livestock;

import java.util.Random;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import net.dries007.tfc.config.animals.MammalConfig;
import net.dries007.tfc.util.calendar.Calendars;

public interface MammalProperties extends TFCAnimalProperties
{
    MammalConfig getMammalConfig();

    long getPregnantTime();

    void setPregnantTime(long time);

    default void tickPregnancy()
    {
        Level level = getEntity().level;
        if (!level.isClientSide && level.getGameTime() % 20 == 0)
        {
            if (getPregnantTime() > 0 && Calendars.SERVER.getTotalDays() >= getPregnantTime() + getGestationDays() && isFertilized())
            {
                birthChildren();
                setFertilized(false);
                setPregnantTime(-1L);
                addUses(10);
            }
        }
    }

    default void birthChildren()
    {
        LivingEntity entity = getEntity();
        if (entity.level instanceof ServerLevel server && entity instanceof AgeableMob ageable)
        {
            Random random = entity.getRandom();
            final int kids = Mth.nextInt(random, 1, getChildCount());
            for (int i = 0; i < kids; i++)
            {
                AgeableMob offspring = ageable.getBreedOffspring(server, ageable);
                if (offspring == null) continue;
                if (offspring instanceof TFCAnimal animal)
                {
                    animal.setPos(entity.position());
                    animal.setFamiliarity(getFamiliarity() < 0.9F ? getFamiliarity() / 2.0F : getFamiliarity() * 0.9F);
                    server.addFreshEntity(animal);
                }
            }
        }
    }


    @Override
    default boolean isReadyToMate()
    {
        return getPregnantTime() <= 0 &&TFCAnimalProperties.super.isReadyToMate();
    }

    @Override
    default void onFertilized(TFCAnimalProperties male)
    {
        //Mark the day this female became pregnant
        setFertilized(true);
        setPregnantTime(Calendars.get(male.getEntity().level).getTotalDays());
    }

    @Override
    default void showExtraClickInfo(Player player)
    {
        if (isFertilized())
        {
            player.displayClientMessage(new TranslatableComponent("tfc.tooltip.animal.pregnant", getGenderedTypeName().getString()), true);
        }
    }

    default int getChildCount()
    {
        return getMammalConfig().childCount().get();
    }

    default long getGestationDays()
    {
        return getMammalConfig().gestationDays().get();
    }
}