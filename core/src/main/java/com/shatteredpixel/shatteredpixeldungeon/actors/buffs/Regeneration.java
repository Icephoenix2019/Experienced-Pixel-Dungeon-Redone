/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2020 Trashbox Bobylev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.watabou.utils.Bundle;

public class Regeneration extends Buff {
	
	{
		//unlike other buffs, this one acts after the hero and takes priority against other effects
		//healing is much more useful if you get some of it off before taking damage
		actPriority = HERO_PRIO - 1;
	}
	
	private static final float REGENERATION_DELAY = 10;
	private float partialHP = 0;

	private final String PART_HP = "partialHP";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(PART_HP, partialHP);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(PART_HP))
			partialHP = bundle.getFloat(PART_HP);
	}

	@Override
	public boolean act() {
		if (target.isAlive()) {
			ChaliceOfBlood.chaliceRegen regenBuff = Dungeon.hero.buff( ChaliceOfBlood.chaliceRegen.class);
			float regen = REGENERATION_DELAY;
			if (regenBuff != null) {
				if (regenBuff.isCursed()) {
					regen *= 1.5f;
				} else {
					if (regenBuff.itemLevel() < 11)
						regen -= regenBuff.itemLevel()*0.9f;
					else regen = 1 / (float)((3 * Math.pow(regenBuff.itemLevel() - 1, 2) / 243));
					regen /= RingOfEnergy.artifactChargeMultiplier(target);
				}
			}
			regen = 1f / regen;

			if (target.HP < regencap() && !((Hero)target).isStarving()) {
				LockedFloor lock = target.buff(LockedFloor.class);
				if (target.HP > 0 && (lock == null || lock.regenOn())) {
					partialHP += regen;
					if (partialHP >= 1){
						target.HP = Math.min(target.HP + (int)partialHP, target.HT);
						partialHP -= (int)partialHP;
						if (target.HP == regencap()) {
							((Hero) target).resting = false;
						}
					}
				}
			}

			spend( TICK );
			
		} else {
			
			diactivate();
			
		}
		
		return true;
	}
	
	public int regencap(){
		return target.HT;
	}
}
