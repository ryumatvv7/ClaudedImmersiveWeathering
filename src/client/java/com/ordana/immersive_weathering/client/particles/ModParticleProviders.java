package com.ordana.immersive_weathering.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.GlowParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Client particles of the mod, rewritten for the 26.1 particle API (single sprite passed to the particle,
 * render "layer" instead of render type). Behaviour follows the original Ember / Leaf / NormalGravity particles.
 */
public class ModParticleProviders {

    private abstract static class Base extends SingleQuadParticle {
        protected Base(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, TextureAtlasSprite sprite) {
            super(level, x, y, z, xd, yd, zd, sprite);
        }

        @Override
        protected Layer getLayer() {
            return Layer.OPAQUE;
        }
    }

    // --- ember spark: tiny, short lived, slowly rising

    private static class Ember extends Base {
        Ember(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite, RandomSource random) {
            super(level, x, y, z, 0, 0, 0, sprite);
            this.setSize(0.005F, 0.005F);
            this.quadSize *= random.nextFloat() * 0.6F + 1F;
            this.lifetime = (int) (10.0D + (Mth.randomBetween(random, 0.1f, 0.2f)));
            this.hasPhysics = false;
            this.friction = 0.0F;
            this.gravity = -0.005F;
        }
    }

    public record EmberProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xAux, double yAux, double zAux, RandomSource random) {
            return new Ember(level, x, y, z, sprites.get(random), random);
        }
    }

    // --- falling leaf / flower petal: spins while falling, lies flat on the ground

    private static class Leaf extends Base {
        private final float rotationSpeed;

        Leaf(ClientLevel level, double x, double y, double z, double yd, TextureAtlasSprite sprite, RandomSource random) {
            super(level, x, y, z, 0, yd, 0, sprite);
            this.setSize(1.0F, 1.0F);
            this.quadSize *= random.nextFloat() * 0.6F + 0.4F;
            this.lifetime = (int) (16.0D / (random.nextDouble() * 0.8D + 0.2D));
            this.hasPhysics = true;
            this.friction = 1.0F;
            this.gravity = 1.0F;
            this.rotationSpeed = (random.nextFloat() - 0.5F) * 0.1F;
        }

        @Override
        public void tick() {
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            if (this.age++ >= this.lifetime) {
                this.remove();
            } else {
                this.oRoll = this.roll;
                this.roll += Mth.PI * this.rotationSpeed * 2.0F;
                if (this.onGround) {
                    this.oRoll = this.roll = 0.0F;
                }
                this.move(this.xd, this.yd, this.zd);
                this.yd -= 0.003F;
                this.yd = Math.max(this.yd, -0.14F);
            }
        }
    }

    public record LeafProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xAux, double yAux, double zAux, RandomSource random) {
            double vel = xAux == 0 ? -3 : xAux;
            return new Leaf(level, x, y, z, vel, sprites.get(random), random);
        }
    }

    // --- simple sprite with normal gravity (moss, azalea flowers on shearing)

    private static class NormalGravity extends Base {
        NormalGravity(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite, RandomSource random) {
            super(level, x, y, z, 0, 0, 0, sprite);
            this.setSize(1.0F, 1.0F);
            this.quadSize *= random.nextFloat() * 0.6F + 0.4F;
            this.gravity = 1.0F;
        }
    }

    public record NormalGravityProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xAux, double yAux, double zAux, RandomSource random) {
            return new NormalGravity(level, x, y, z, sprites.get(random), random);
        }
    }

    // --- rust scraping: vanilla scrape particle recolored

    public static class ScrapeRustProvider extends GlowParticle.ScrapeProvider {
        public ScrapeRustProvider(SpriteSet sprites) {
            super(sprites);
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xAux, double yAux, double zAux, RandomSource random) {
            Particle p = super.createParticle(type, level, x, y, z, xAux, yAux, zAux, random);
            if (p instanceof SingleQuadParticle q) {
                if (random.nextBoolean()) {
                    q.setColor(196 / 255f, 118 / 255f, 73 / 255f);
                } else {
                    q.setColor(176 / 255f, 63 / 255f, 40 / 255f);
                }
            }
            return p;
        }
    }
}
