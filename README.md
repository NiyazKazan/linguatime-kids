# LinguaTime Kids

LinguaTime Kids — Android-приложение для детей и родителей, где ребёнок изучает английский язык с ИИ-агентом и получает баллы, которые можно обменять на разрешённое экранное время.

## Статус

MVP specification / preparation stage.

## Roles

- Parent: registration, child profile, progress tracking, screen time rules.
- Child: English learning, AI tutor, points, reward requests.

## Platform

Android, single app with two modes: Parent and Child.

## MVP Scope

- Text + audio English learning.
- Initial level assessment.
- AI tutor assistance.
- Points for learning.
- Parent-controlled screen time rewards.
- App-level parental control, not full Android system unlocking.

## Documents

- docs/TZ.md — technical specification.
- docs/MVP-SCREENS.md — MVP screen list.

## Important Security Rules

- No API keys in this repository.
- No passwords in this repository.
- No private user data in this repository.
- AI provider keys must be stored only in backend/CI secrets.
