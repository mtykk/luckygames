package io.github.mtykk.luckygames;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;

import java.util.Locale;
import java.util.ResourceBundle;

public class PluginTranslator {
    private final static MiniMessageTranslationStore store = MiniMessageTranslationStore.create(Key.key("lucky_games:translator"));
    private static void registerTranslationStore(String bundleBaseName, Locale locale){
        ResourceBundle bundle = ResourceBundle.getBundle(bundleBaseName, locale);
        store.registerAll(locale, bundle, false);
    }
    PluginTranslator(){
        //Load default language
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.ROOT);
        store.registerAll(Locale.ROOT, bundle, false);
        store.registerAll(Locale.ENGLISH, bundle, false);

        //Load languages
        registerTranslationStore("messages",Locale.SIMPLIFIED_CHINESE);

        store.defaultLocale(Locale.ROOT);
        GlobalTranslator.translator().addSource(store);
    }
}
