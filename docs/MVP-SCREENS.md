# LinguaTime Kids — MVP Screens

## Common Screens

| Code | Screen | Purpose |
|---|---|---|
| S-01 | Splash | App loading |
| S-02 | Role Selection | Choose Parent or Child |
| S-03 | Parent Gate | Check parent PIN before parent zone |
| S-04 | Error Screen | Network or server error |
| S-05 | Update Required | App update required |

## Parent Screens

| Code | Screen | Purpose |
|---|---|---|
| P-01 | Parent Sign In | Parent login |
| P-02 | Parent Sign Up | Parent registration |
| P-03 | Parent Consent | Consent for child data processing |
| P-04 | Parent PIN Setup | Create parent PIN |
| P-05 | Parent Home | Main parent dashboard |
| P-06 | Children List | List of child profiles |
| P-07 | Add Child | Create child profile |
| P-08 | Child Profile | Child details |
| P-09 | Device Linking | Link child device |
| P-10 | Progress Dashboard | English learning progress |
| P-11 | Level Report | Current level and skills |
| P-12 | Points History | Points transactions |
| P-13 | Screen Time Settings | Screen time rules |
| P-14 | Reward Categories | Allowed reward categories |
| P-15 | Time Requests | Child time requests |
| P-16 | Time Grants | Approved screen time |
| P-17 | Parent Settings | Account and security settings |
| P-18 | Privacy Policy | Privacy policy |
| P-19 | Delete Account | Delete account and data |

## Child Screens

| Code | Screen | Purpose |
|---|---|---|
| C-01 | Child Login | Login by code or QR |
| C-02 | Child Onboarding | Intro for child |
| C-03 | Avatar Setup | Choose avatar or character name |
| C-04 | Placement Test Intro | Explain first test |
| C-05 | Placement Test | Initial English level test |
| C-06 | Test Result | Start level result |
| C-07 | Child Home | Main child screen |
| C-08 | Daily Lesson Card | Today lesson card |
| C-09 | Lesson Flow | Lesson passing flow |
| C-10 | Exercise Screen | Exercise screen |
| C-11 | Audio Player | Listen to word or phrase |
| C-12 | Answer Feedback | Correct or incorrect |
| C-13 | Lesson Result | Lesson result |
| C-14 | Points Balance | Points balance |
| C-15 | Reward Request | Request screen time |
| C-16 | Reward Pending | Waiting parent approval |
| C-17 | Reward Approved | Time approved |
| C-18 | Reward Timer | Available time timer |
| C-19 | Streak Screen | Learning streak |
| C-20 | Child Exit Gate | Exit from child mode |

## Important Limitation

This MVP does not unlock Android system lock screen.

Parental control is implemented as:

- app-level child mode;
- parent PIN protection;
- screen time requests;
- parent approval;
- server-side time accounting.

Strong system-level control may be added later through Android Enterprise / Device Owner / managed device mode.
