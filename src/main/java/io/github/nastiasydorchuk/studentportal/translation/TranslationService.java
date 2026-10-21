package io.github.nastiasydorchuk.studentportal.translation;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.*;
import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCard;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceItem;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceCategory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TranslationService {
    private final Translate translate;

    public TranslationService(Translate translate) {
        this.translate = translate;
    }

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

        // 4. Translate the text fields of each item; keep URLs, icons, emails etc. exactly the same
        if (englishCategory.getItems() != null) {
            localizedCategory.setItems(englishCategory.getItems().stream().map(item -> new ResourceItem(
                    translateText(item.title(), targetLanguage),
                    item.icon(),
                    translateText(item.description(), targetLanguage),
                    translateText(item.actionText(), targetLanguage),
                    item.url(),
                    translateText(item.label(), targetLanguage),
                    item.badgeClass(),
                    item.location(),
                    item.contact()
            )).toList());
        }

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

    public List<PortalCard> translatePortalCards(List<PortalCard> englishCards, String targetLanguage, String sourceHash) {
        if (englishCards == null) return List.of();

        // Translate the display text; NEVER translate internal routes or icons!
        return englishCards.stream().map(card -> new PortalCard(
                card.cardId() + "_" + targetLanguage, // e.g., "academic_es"
                translateText(card.title(), targetLanguage),
                card.route(),
                card.icon(),
                translateText(card.description(), targetLanguage),
                card.cardId(),
                targetLanguage,
                sourceHash
        )).toList();
    }

    public OnboardingSteps translateSteps(OnboardingSteps source, String lang, String sourceHash) {
        List<Step> steps = source.steps() != null
                ? source.steps().stream().map(s -> new Step(
                        s.order(),
                        translateText(s.summary(), lang),
                        translateText(s.description(), lang),
                        translateText(s.actionText(), lang),
                        s.url()
                )).toList()
                : null;

        return new OnboardingSteps("steps_" + lang, lang, steps, sourceHash);
    }

    public CohortDocument translateCohort(CohortDocument c, String lang, String sourceHash) {
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
                c.event().eligibility() != null
                        ? c.event().eligibility().stream().map(e -> translateText(e, lang)).toList()
                        : null,
                translateText(c.event().contactInfo(), lang),
                reg)
                : null;

        return new CohortDocument(
                c.level().name() + "_" + lang, // e.g., "undergraduate_es"
                c.level(),
                lang,
                translateText(c.title(), lang),
                dates,
                event,
                sourceHash);
    }

    public InductionEvents translateInductionEvents(InductionEvents source, String lang, String sourceHash) {
        List<InductionEvent> events = source.events() != null
                ? source.events().stream().map(e -> new InductionEvent(
                        translateText(e.title(), lang),
                        e.icon(),
                        translateText(e.desc(), lang),
                        translateText(e.actionText(), lang),
                        e.url()
                )).toList()
                : null;

        return new InductionEvents("events_" + lang, lang, events, sourceHash);
    }
}