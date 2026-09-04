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
