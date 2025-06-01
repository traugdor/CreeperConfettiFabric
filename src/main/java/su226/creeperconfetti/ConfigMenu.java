package su226.creeperconfetti;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

@Environment(EnvType.CLIENT)
public class ConfigMenu implements ModMenuApi {
  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    if (!FabricLoader.getInstance().isModLoaded("cloth-config2")) {
      ConsoleLogger.LOGGER.warn("Couldn't find Cloth Config, config menu disabled!");
      return parent -> null;
    }
    return parent -> createConfigScreen(parent);
  }
  
  private Screen createConfigScreen(Screen parent) {
    ConfigBuilder builder = ConfigBuilder.create()
      .setParentScreen(parent)
      .setTitle(Text.translatable("title.creeperconfetti.config"));
    builder.setSavingRunnable(Config::serialize);
    ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.creeperconfetti.general"));
    ConfigEntryBuilder entryBuilder = builder.entryBuilder();
    general.addEntry(entryBuilder.startFloatField(Text.translatable("option.creeperconfetti.chance"), Config.chance)
      .setMin(0)
      .setMax(1)
      .setTooltip(Text.translatable("option.creeperconfetti.chance.description"))
      .setSaveConsumer(value -> Config.chance = value)
      .build());
    general.addEntry(entryBuilder.startFloatField(Text.translatable("option.creeperconfetti.damage"), Config.damage)
      .setMin(0)
      .setMax(1)
      .setTooltip(Text.translatable("option.creeperconfetti.damage.description"))
      .setSaveConsumer(value -> Config.damage = value)
      .build());
    general.addEntry(entryBuilder.startFloatField(Text.translatable("option.creeperconfetti.soundChance"), Config.soundChance)
      .setMin(0)
      .setMax(1)
      .setTooltip(Text.translatable("option.creeperconfetti.soundChance.description"))
      .setSaveConsumer(value -> Config.soundChance = value)
      .build());
    return builder.build();
  }
}
