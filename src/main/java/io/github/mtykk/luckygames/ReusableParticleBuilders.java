package io.github.mtykk.luckygames;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Particle;
import org.bukkit.Color;

public class ReusableParticleBuilders {
    static ParticleBuilder GREEN_EFFECT_SPREAD_PARTICLE_BUILDER;
    static ParticleBuilder POOF_PARTICLE_BUILDER;
    static ParticleBuilder DAMAGE_PARTICLE_BUILDER;

    static {
        GREEN_EFFECT_SPREAD_PARTICLE_BUILDER = Particle.INSTANT_EFFECT.builder().data(new Particle.Spell(Color.fromRGB(51,209,122),2.0f)).extra(0.5).offset(0.25,0.25,0.25).force(true);
        POOF_PARTICLE_BUILDER = Particle.POOF.builder().extra(0.1).offset(0.25,0.25,0.25).count(16).force(true);
        DAMAGE_PARTICLE_BUILDER = Particle.DAMAGE_INDICATOR.builder().extra(0.1).offset(0.25,0.25,0.25).count(32).force(true);
    }
}
