import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import fetch from "node-fetch";

// Инициализация Firebase Admin
admin.initializeApp();

// Интерфейсы
interface Exercise {
  type: string;
  question: string;
  options: string[];
  correctAnswer: string;
  points: number;
  explanation: string;
}

interface LessonResponse {
  exercises: Exercise[];
}

// Системный промпт для Qwen
const SYSTEM_PROMPT = `Ты — дружелюбный учитель английского для детей 8-14 лет.
Твоя цель — довести ученика до уровня C2.

Сгенерируй 3 задания типа multiple_choice на английском языке.
Сложность должна соответствовать уровню {LEVEL}.

Верни ТОЛЬКО JSON в следующем формате:
{
  "exercises": [
    {
      "type": "multiple_choice",
      "question": "Вопрос на английском",
      "options": ["вариант1", "вариант2", "вариант3", "вариант4"],
      "correctAnswer": "правильный ответ",
      "points": 5,
      "explanation": "Объяснение на русском, почему это правильный ответ"
    }
  ]
}

Не добавляй никакого текста кроме JSON!`;

// Cloud Function для генерации урока
export const generateLesson = functions.https.onCall(async (data, context) => {
  // Проверка авторизации
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  const { level, childId } = data;
  
  if (!level || !childId) {
    throw new functions.https.HttpsError('invalid-argument', 'Missing level or childId');
  }

  try {
    // Получаем токен Hugging Face из переменных окружения
    const hfToken = process.env.HUGGING_FACE_TOKEN;
    
    if (!hfToken) {
      throw new Error('Hugging Face token not configured');
    }

    // Формируем промпт
    const userPrompt = SYSTEM_PROMPT.replace('{LEVEL}', level);

    // Запрос к Qwen через Hugging Face API
    const response = await fetch(
      'https://api-inference.huggingface.co/models/Qwen/Qwen2.5-7B-Instruct/v1/chat/completions',
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${hfToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          model: 'Qwen/Qwen2.5-7B-Instruct',
          messages: [
            { role: 'system', content: SYSTEM_PROMPT },
            { role: 'user', content: `Сгенерируй урок для уровня ${level}` }
          ],
          temperature: 0.7,
          max_tokens: 1000,
        }),
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Hugging Face API error: ${errorText}`);
    }

    const result = await response.json();
    
    // Парсим ответ от Qwen
    const content = result.choices?.[0]?.message?.content;
    
    if (!content) {
      throw new Error('Empty response from Qwen');
    }

    // Пытаемся распарсить JSON
    let lessonData: LessonResponse;
    try {
      lessonData = JSON.parse(content);
    } catch (parseError) {
      throw new Error('Failed to parse Qwen response as JSON');
    }

    // Сохраняем урок в Firestore
    const lessonRef = admin.firestore().collection('lessons').doc();
    await lessonRef.set({
      title: `Урок уровня ${level}`,
      level: level,
      description: `Автоматически сгенерированный урок`,
      exercises: lessonData.exercises.map(ex => ({
        type: ex.type,
        question: ex.question,
        options: ex.options,
        correctAnswer: ex.correctAnswer,
        points: ex.points,
        explanation: ex.explanation || ''
      })),
      generatedBy: 'Qwen',
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      childId: childId
    });

    return {
      lessonId: lessonRef.id,
      exercises: lessonData.exercises
    };

  } catch (error) {
    console.error('Error generating lesson:', error);
    throw new functions.https.HttpsError('internal', 'Failed to generate lesson');
  }
});
