# Project Guidelines & Persistent Rules

## 1. Firestore Rules Maintenance (MANDATORY)
- **Always preserve and maintain `firestore.rules`** in the project root.
- Whenever any new feature, collection, sub-collection, or database write operation is added to the Android app, **immediately update `firestore.rules`** with appropriate matching security permissions for that new collection/field.
- **Admin/Staff Privileges:**
  - Owner emails: `omiq0534@gmail.com`, `6375615586@fam`, `admin@esports.com`
  - Owner UID: `13SgN4yvKvfXWqH64hR9Z1XzD442`
  - Staff / Moderators: Users with `isModerator == true` or `role in ['moderator', 'admin', 'owner']`
- **Users Security:**
  - Users are allowed to update their profile and normal coin/cash balances for store purchases and match fees.
  - Sensitive administrative fields (`role`, `isModerator`, `isBanned`, `banType`, `banReason`, `banUntil`, `hasXBadge`) must always remain protected from tampering by regular users.
  - All user sub-collections (`inventory`, `purchased_cards`, `queries`, `notifications`) must grant full read/write access to the user and staff.

## 2. Hardcore Architecture, Clean UI & Proactive Innovation (MANDATORY)
- **Proactive Expert Advice & Honest Feedback:** In every task, actively evaluate the UI/UX, visual clutter, performance, and architecture. Suggest modern solutions (what looks cluttered, what could be made cleaner, or what advanced techniques like Canvas shaders, 3D physics, or micro-interactions can elevate the app).
- **Hardcore Coding Standards:** Build robust, highly optimized, lag-free Compose architectures with clean spacing, strict typography, and deliberate color control. Avoid 'AI slop' or overcrowded interfaces.
- **Clean Aesthetic Discipline:** Keep interfaces modern, spacious, and focused on core user actions (e.g., Match Time, Entry Fee, Prize Pool, Join Button).

## 3. Role & Search-First UI Workflow (MANDATORY)
- **Role:** Senior Android UI/UX Architect & Jetpack Compose Developer.
- **Search-First UI Workflow:**
  1. **Live Benchmark Search:** For every UI request, feature, or screen, search the web for how the top-rated apps, leading industry products, and award-winning designs implement that exact feature.
  2. **Pattern Analysis:** Analyze the searched real-world apps to extract their UX layout hierarchy, typography scaling, surface contrast, component structure, and visual polish.
  3. **Adaptive Synthesis:** Never enforce arbitrary hardcoded colors, themes, or rigid templates. Build the visual language entirely based on the searched benchmarks and the user's specific requirements.
  4. **Production Code:** Translate the researched modern design directly into clean, modular Jetpack Compose Material 3 code with proper layout hierarchy, elevation, and readable states.
