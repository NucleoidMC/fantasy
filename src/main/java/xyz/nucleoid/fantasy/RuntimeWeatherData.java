package xyz.nucleoid.fantasy;

import net.minecraft.world.level.saveddata.WeatherData;

public class RuntimeWeatherData extends WeatherData {
    final RuntimeWorldConfig config;

    public RuntimeWeatherData(RuntimeWorldConfig config) {
        this.config = config;
    }

    @Override
    public int getClearWeatherTime() {
        return this.config.getSunnyTime();
    }

    @Override
    public int getRainTime() {
        return this.config.getRainTime();
    }

    @Override
    public int getThunderTime() {
        return this.config.getThunderTime();
    }

    @Override
    public boolean isRaining() {
        return this.config.isRaining();
    }

    @Override
    public boolean isThundering() {
        return this.config.isThundering();
    }

    @Override
    public void setClearWeatherTime(int clearWeatherTime) {
        this.config.setSunny(clearWeatherTime);
    }

    @Override
    public void setRaining(boolean raining) {
        this.config.setRaining(raining);
    }

    @Override
    public void setRainTime(int rainTime) {
        this.config.setRaining(rainTime);
    }

    @Override
    public void setThundering(boolean thundering) {
        this.config.setThundering(thundering);
    }

    @Override
    public void setThunderTime(int thunderTime) {
        this.config.setThundering(thunderTime);
    }
}
