import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import fetch from "node-fetch";

admin.initializeApp();

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

interface GenerateLessonData {
  level: string;
  childId: string;
}

const SYSTEM_PROMPT = `Ты — дружелюбный учитель английского для детей 8-14 лет.
Твоя цель — довести ученика до уровня C2.

Сгенерируй 3 задания типа multiple_choice на английском языке.
Сложность должна соответствовать уровню {LEVEL}.

Верни ТОЛЬКО JSON в следующем формате (без markdown, без пояснений):
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
}`;

export const generateLesson = functions.https.onCall(
  async (data: GenerateLessonData, context: functions.https.CallableContext) => {
    if (!context.auth) {
      throw new functions.https.HttpsError(
        'unauthenticated',
        'User must be authenticated'
      );
    }

    const { level, childId } = data;

    if (!level || !childId) {
      throw new functions.https.HttpsError(
        'invalid-argument',
        'Missing level or childId'
      );
    }

    try {
      const hfToken = process.env.HUGGING_FACE_TOKEN;

      if (!hfToken) {
        throw new Error('Hugging Face token not configured');
      }

      const finalPrompt = SYSTEM_PROMPT.replace('{LEVEL}', level);

      const response = await fetch(
        'https://api-inference.huggingface.co/models/Qwen/Qwen3.8-2.4T-A95B/v1/chat/completions',
        {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${hfToken}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            model: 'Qwen/Qwen3.8-2.4T-A95B',
            messages: [
              { role: 'system', content: finalPrompt },
              { role: 'user', content: `Сгенерируй урок для уровня ${level}` },
            ],
            temperature: 0.7,
            max_tokens: 1000,
          }),
        }
      );

      if (!response.ok) {
        const errorText = await response.text();
        console.error('HF API error:', response.status, errorText);
        throw new Error(
          `Hugging Face API error: ${response.status} ${errorText}`
        );
      }

      const result = await response.json();

      const content = result.choices?.[0]?.message?.content;

      if (!content) {
        throw new Error('Empty response from Qwen');
      }

      console.log('Qwen response:', content.substring(0, 500));

      let jsonStr = content;
      const jsonMatch = content.match(/\{[\s\S]*\}/);
      if (jsonMatch) {
        jsonStr = jsonMatch[0];
      }

      let lessonData: LessonResponse;
      try {
        lessonData = JSON.parse(jsonStr);
      } catch (parseError) {
        console.error('Parse error:', parseError, 'Content:', content);
        throw new Error('Failed to parse Qwen response as JSON');
      }

      if (!lessonData.exercises || !Array.isArray(lessonData.exercises)) {
        throw new Error('Invalid lesson structure from Qwen');
      }

      const lessonRef = admin.firestore().collection('lessons').doc();
      await lessonRef.set({
        title: `Урок уровня ${level}`,
        level: level,
        description: `Автоматически сгенерированный урок`,
        exercises: lessonData.exercises.map((ex) => ({
          type: ex.type,
          question: ex.question,
          options: ex.options,
          correctAnswer: ex.correctAnswer,
          points: ex.points,
          explanation: ex.explanation || '',
        })),
        generatedBy: 'Qwen3.8',
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        childId: childId,
      });

      return {
        lessonId: lessonRef.id,
        exercises: lessonData.exercises,
      };
    } catch (error: any) {
      console.error('Error generating lesson:', error);
      throw new functions.https.HttpsError(
        'internal',
        error.message || 'Failed to generate lesson'
      );
    }
  }
);
