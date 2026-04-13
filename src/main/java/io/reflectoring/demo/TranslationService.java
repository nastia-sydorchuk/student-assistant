package io.reflectoring.demo;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import io.reflectoring.demo.dto.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TranslationService {
    private final String GOOGLE_API_KEY = "";

    // Tell Java to build the translator using that specific key
    private final Translate translate = TranslateOptions.newBuilder()
            .setApiKey(GOOGLE_API_KEY)
            .build()
            .getService();

    // Initialize the Google Translate Client
    //  private final Translate translate = TranslateOptions.getDefaultInstance().getService();

    public ResourceCategory translateCategory(ResourceCategory englishCategory, String targetLanguage) {

        // 1. Create a new empty category for the new language
        ResourceCategory localizedCategory = new ResourceCategory();

        // 2. Set the IDs (e.g., ID becomes "academic_es")
        localizedCategory.setId(englishCategory.getCategoryId() + "_" + targetLanguage);
        localizedCategory.setCategoryId(englishCategory.getCategoryId());
        localizedCategory.setLanguage(targetLanguage);

        // 3. Translate the main category text
        localizedCategory.setCategoryTitle(translateText(englishCategory.getCategoryTitle(), targetLanguage));
        localizedCategory.setDescription(translateText(englishCategory.getDescription(), targetLanguage));

        // 4. Loop through the items and translate them
        List<ResourceItem> localizedItems = new ArrayList<>();
        for (ResourceItem item : englishCategory.getItems()) {
            ResourceItem localizedItem = new ResourceItem();

            // Translate the text fields
            localizedItem.setTitle(translateText(item.getTitle(), targetLanguage));
            localizedItem.setDescription(translateText(item.getDescription(), targetLanguage));

            // Only translate Action Text if it exists
            if (item.getActionText() != null) {
                localizedItem.setActionText(translateText(item.getActionText(), targetLanguage));
            }

            // Keep the non-translatable fields exactly the same (URLs, Icons, Emails)
            localizedItem.setIcon(item.getIcon());
            localizedItem.setUrl(item.getUrl());
            localizedItem.setContact(item.getContact());

            localizedItems.add(localizedItem);
        }

        localizedCategory.setItems(localizedItems);
        return localizedCategory;
    }

    // Helper method to call Google API safely
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

            // Set the IDs safely (e.g., "academic_es")
            localizedCard.setId(card.getCardId() + "_" + targetLanguage);
            localizedCard.setCardId(card.getCardId());
            localizedCard.setLanguage(targetLanguage);

            // NEVER translate internal routes or icons!
            localizedCard.setRoute(card.getRoute());
            localizedCard.setIcon(card.getIcon());

            // Translate the display text
            localizedCard.setTitle(translateText(card.getTitle(), targetLanguage));
            localizedCard.setDescription(translateText(card.getDescription(), targetLanguage));

            translatedCards.add(localizedCard);
        }

        return translatedCards;
    }

    public Onboarding translateOnboarding(Onboarding source, String lang) {
        Onboarding translated = new Onboarding();
        translated.setId(source.getConfigId() + "_" + lang);
        translated.setConfigId(source.getConfigId());
        translated.setLanguage(lang);

        if (source.getSteps() != null) {
            translated.setSteps(source.getSteps().stream().map(s -> new StepDTO(
                    s.order(),
                    translateText(s.summary(), lang),
                    translateText(s.description(), lang),
                    translateText(s.actionText(), lang),
                    s.url()
            )).toList());
        }

        if (source.getCohorts() != null) {
            translated.setCohorts(source.getCohorts().stream().map(c -> {
                List<ImportantDate> dates = c.importantDates() != null
                        ? c.importantDates().stream().map(d -> new ImportantDate(
                        translateText(d.activity(), lang),
                        d.date(),
                        d.type()
                )).toList()
                        : null;

                RegistrationLink reg = c.event() != null && c.event().registration() != null
                        ? new RegistrationLink(
                        translateText(c.event().registration().label(), lang),
                        c.event().registration().link())
                        : null;

                EventDetails event = c.event() != null
                        ? new EventDetails(
                        translateText(c.event().title(), lang),
                        translateText(c.event().description(), lang),
                        c.event().programmeOutline() != null
                                ? c.event().programmeOutline().stream().map(p -> translateText(p, lang)).toList()
                                : null,
                        translateText(c.event().eligibility(), lang),
                        c.event().eligibilityList() != null
                                ? c.event().eligibilityList().stream().map(e -> translateText(e, lang)).toList()
                                : null,
                        translateText(c.event().contactInfo(), lang),
                        reg)
                        : null;

                return new Cohort(c.level(), translateText(c.title(), lang), dates, event);
            }).toList());
        }

        if (source.getInductionEvents() != null) {
            translated.setInductionEvents(source.getInductionEvents().stream().map(e -> new InductionEvent(
                    translateText(e.title(), lang),
                    e.icon(),
                    translateText(e.desc(), lang),
                    translateText(e.actionText(), lang),
                    e.url()
            )).toList());
        }

        return translated;
    }
}