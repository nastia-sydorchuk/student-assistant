const fs = require('fs');
const { Translate } = require('@google-cloud/translate').v2;

if (!process.env.GOOGLE_TRANSLATE_KEY) {
    console.error('GOOGLE_TRANSLATE_KEY environment variable is not set.');
    process.exit(1);
}

const translate = new Translate({ key: process.env.GOOGLE_TRANSLATE_KEY });

const i18nPath = './src/assets/';
const sourceFile = 'en.json';
const targetLanguages = ['es', 'fr', 'de'];

async function runAutoTranslator() {
    console.log('Starting auto-translation...');

    try {
        const rawData = fs.readFileSync(i18nPath + sourceFile, 'utf8');
        const englishData = JSON.parse(rawData);

        async function translateObject(obj, targetLang) {
            const translatedObj = {};
            for (const key in obj) {
                if (typeof obj[key] === 'object' && obj[key] !== null) {
                    translatedObj[key] = await translateObject(obj[key], targetLang);
                } else {
                    const [translation] = await translate.translate(obj[key], targetLang);
                    translatedObj[key] = translation;
                }
            }
            return translatedObj;
        }

        for (const lang of targetLanguages) {
            console.log(`Translating to [${lang.toUpperCase()}]...`);
            const translatedData = await translateObject(englishData, lang);
            
            fs.writeFileSync(
                `${i18nPath}${lang}.json`, 
                JSON.stringify(translatedData, null, 2)
            );
            console.log(`Successfully generated ${lang}.json!`);
        }
        
        console.log('All translations complete!');

    } catch (error) {
        console.error('Error during translation:', error);
    }
}

runAutoTranslator();