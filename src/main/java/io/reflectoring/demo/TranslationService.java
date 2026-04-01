package io.reflectoring.demo;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import io.reflectoring.demo.dto.PortalCard;
import io.reflectoring.demo.dto.ResourceCategory;
import io.reflectoring.demo.dto.ResourceItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TranslationService {
    private final String GOOGLE_API_KEY = "";

    private final Translate translate = TranslateOptions.newBuilder()
            .setApiKey(GOOGLE_API_KEY)
            .build()
            .getService();

    public ResourceCategory translateCategory(ResourceCategory englishCategory, String targetLanguage) {

        ResourceCategory localizedCategory = new ResourceCategory();

        localizedCategory.setId(englishCategory.getCategoryId() + "_" + targetLanguage);
        localizedCategory.setCategoryId(englishCategory.getCategoryId());
        localizedCategory.setLanguage(targetLanguage);

        localizedCategory.setCategoryTitle(translateText(englishCategory.getCategoryTitle(), targetLanguage));
        localizedCategory.setDescription(translateText(englishCategory.getDescription(), targetLanguage));

        List<ResourceItem> localizedItems = new ArrayList<>();
        for (ResourceItem item : englishCategory.getItems()) {
            ResourceItem localizedItem = new ResourceItem();

            localizedItem.setTitle(translateText(item.getTitle(), targetLanguage));
            localizedItem.setDescription(translateText(item.getDescription(), targetLanguage));

            if (item.getActionText() != null) {
                localizedItem.setActionText(translateText(item.getActionText(), targetLanguage));
            }

            localizedItem.setIcon(item.getIcon());
            localizedItem.setUrl(item.getUrl());
            localizedItem.setContact(item.getContact());

            localizedItems.add(localizedItem);
        }

        localizedCategory.setItems(localizedItems);
        return localizedCategory;
    }

    private String translateText(String text, String targetLanguage) {
        if (text == null || text.isEmpty()) return text;

        Translation translation = translate.translate(
                text,
                Translate.TranslateOption.sourceLanguage("en"),
                Translate.TranslateOption.targetLanguage(targetLanguage)
        );
        return translation.getTranslatedText();
    }

    public List<PortalCard> translatePortalCards(List<PortalCard> englishCards, String targetLanguage) {
        List<PortalCard> translatedCards = new ArrayList<>();

        for (PortalCard card : englishCards) {
            PortalCard localizedCard = new PortalCard();

            localizedCard.setId(card.getCardId() + "_" + targetLanguage);
            localizedCard.setCardId(card.getCardId());
            localizedCard.setLanguage(targetLanguage);

            localizedCard.setRoute(card.getRoute());
            localizedCard.setIcon(card.getIcon());

            localizedCard.setTitle(translateText(card.getTitle(), targetLanguage));
            localizedCard.setDescription(translateText(card.getDescription(), targetLanguage));

            translatedCards.add(localizedCard);
        }

        return translatedCards;
    }
}