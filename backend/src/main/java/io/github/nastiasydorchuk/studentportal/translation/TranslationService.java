package io.github.nastiasydorchuk.studentportal.translation;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.*;
import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCard;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceItem;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceCategory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TranslationService {
    private final Translate translate; // null when no API key is configured

    public TranslationService(Optional<Translate> translate) {
        this.translate = translate.orElse(null);
    }

    // False without an API key; callers then serve English instead of translating
    public boolean isEnabled() {
        return translate != null;
    }

    // Only display text is translated; URLs, icons, locations and contacts are copied unchanged
    public ResourceCategory translateCategory(ResourceCategory englishCategory, String targetLanguage) {
        ResourceCategory localizedCategory = new ResourceCategory();
        localizedCategory.setId(englishCategory.getCategoryId() + "_" + targetLanguage); // e.g. "academic_es"
        localizedCategory.setCategoryId(englishCategory.getCategoryId());
        localizedCategory.setLanguage(targetLanguage);
        localizedCategory.setCategoryTitle(translateText(englishCategory.getCategoryTitle(), targetLanguage));
        localizedCategory.setDescription(translateText(englishCategory.getDescription(), targetLanguage));

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

    // Null and empty strings are returned as-is, without an API call
    private String translateText(String text, String targetLanguage) {
        if (text == null || text.isEmpty()) return text;

        // format("text") stops the API from HTML-escaping the result (e.g. "&" -> "&amp;")
        Translation translation = translate.translate(
                text,
                Translate.TranslateOption.sourceLanguage("en"),
                Translate.TranslateOption.targetLanguage(targetLanguage),
                Translate.TranslateOption.format("text")
        );
        return translation.getTranslatedText();
    }

    public List<PortalCard> translatePortalCards(List<PortalCard> englishCards, String targetLanguage, String sourceHash) {
        if (englishCards == null) return List.of();

        // Routes and icons are identifiers, so only the display text is translated
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
        List<OnboardingStep> steps = source.steps() != null
                ? source.steps().stream().map(s -> new OnboardingStep(
                        s.order(),
                        translateText(s.summary(), lang),
                        translateText(s.description(), lang),
                        translateText(s.actionText(), lang),
                        s.url()
                )).toList()
                : null;

        return new OnboardingSteps("steps_" + lang, lang, steps, sourceHash);
    }

    public Cohort translateCohort(Cohort c, String lang, String sourceHash) {
        List<ImportantDate> dates = c.importantDates() != null
                ? c.importantDates().stream().map(d -> new ImportantDate(
                translateText(d.activity(), lang),
                translateText(d.date(), lang),
                d.type()
        )).toList()
                : null;

        RegistrationLink reg = c.event() != null && c.event().registration() != null
                ? new RegistrationLink(
                translateText(c.event().registration().label(), lang),
                c.event().registration().link())
                : null;

        InductionProgramme event = c.event() != null
                ? new InductionProgramme(
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

        return new Cohort(
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