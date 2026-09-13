# Custom User Rules

- NEVER make unrequested changes, assumptions, or modifications to themes, colors, or features on your own.
- Strictly implement only what the user explicitly asks for.
- When asked for Firebase / Firestore Security Rules, ALWAYS provide the FULL, combined complete rules (Old + New) saved below:

## Master Firestore Security Rules Reference

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // 1. Basic Authentication Check
    function isAuthenticated() {
      return request.auth != null;
    }

    // 2. Supreme Owner Verification (Aapka Admin Email & UID + Secondary Credentials)
    function isOwner() {
      return isAuthenticated() && (
        (request.auth.token.keys().hasAll(['email']) && (
          request.auth.token.email.lower() == 'omiq0534@gmail.com' ||
          request.auth.token.email.lower() == '6375615586@fam' ||
          request.auth.token.email.lower() == 'admin@esports.com'
        )) ||
        request.auth.uid == '13SgN4yvKvfXWqH64hR9Z1XzD442' ||
        (
          exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
          (
            get(/databases/$(database)/documents/users/$(request.auth.uid)).data.get('email', '').lower() == 'omiq0534@gmail.com' ||
            get(/databases/$(database)/documents/users/$(request.auth.uid)).data.get('email', '').lower() == 'admin@esports.com' ||
            get(/databases/$(database)/documents/users/$(request.auth.uid)).data.get('phone', '') == '6375615586' ||
            get(/databases/$(database)/documents/users/$(request.auth.uid)).data.get('role', 'player') in ['owner', 'OWNER']
          )
        )
      );
    }

    // 3. Authorized Staff (Owner + Aapke banaye hue Moderators/Admins)
    function isStaff() {
      return isOwner() || (
        isAuthenticated() && 
        exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
        (
          get(/databases/$(database)/documents/users/$(request.auth.uid)).data.get('isModerator', false) == true ||
          get(/databases/$(database)/documents/users/$(request.auth.uid)).data.get('role', 'player') in [
            'moderator', 'admin', 'owner', 'MODERATOR', 'ADMIN', 'OWNER'
          ]
        )
      );
    }

    // --- LIVE ONLINE USERS & PRESENCE TRACKER ---
    match /app_presence/{userId} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated() && (request.auth.uid == userId || isStaff());
    }

    // --- USERS & INVENTORY / VAULT ---
    match /users/{userId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated() && (request.auth.uid == userId || isStaff());
      allow update: if isStaff() || (
        isAuthenticated() && request.auth.uid == userId &&
        !request.resource.data.diff(resource.data).affectedKeys().hasAny([
          'role', 'isModerator', 'isBanned', 'banType', 'banReason', 'banUntil', 'hasXBadge'
        ])
      );
      allow delete: if isOwner();

      match /inventory/{cardId} {
        allow read, write: if isAuthenticated() && (request.auth.uid == userId || isStaff());
      }
      match /purchased_cards/{cardId} {
        allow read, write: if isAuthenticated() && (request.auth.uid == userId || isStaff());
      }
      match /daily_limits/{limitId} {
        allow read, write: if isAuthenticated() && (request.auth.uid == userId || isStaff());
      }
      match /queries/{queryId} {
        allow read, create: if isAuthenticated() && (request.auth.uid == userId || isStaff());
        allow update, delete: if isStaff();
      }
      match /notifications/{notifId} {
        allow read, write: if isAuthenticated() && (request.auth.uid == userId || isStaff());
      }
    }

    // --- MATCHES & TOURNAMENTS ---
    match /matches/{matchId} {
      allow read: if isAuthenticated();
      allow create, delete: if isStaff();
      allow update: if isStaff() || (
        isAuthenticated() && request.resource.data.diff(resource.data).affectedKeys().hasOnly([
          'bookedSlots', 'joinedPlayers', 'slots', 'joinedUsers'
        ])
      );
      match /{subCollection=**} {
        allow read: if isAuthenticated();
        allow write: if isAuthenticated();
      }
    }

    match /tournaments/{tournamentId} {
      allow read: if true;
      allow write: if isStaff();
      match /participants/{participantId} {
        allow read: if true;
        allow create, update: if isAuthenticated();
        allow delete: if isStaff();
      }
    }

    // --- TRANSACTIONS & DATA RECHARGES ---
    match /transactions/{txId} {
      allow read: if isAuthenticated() && (
        resource.data.userId == request.auth.uid || isStaff()
      );
      allow create: if isStaff() || (
        isAuthenticated() && request.resource.data.userId == request.auth.uid
      );
      allow update: if isStaff() || (
        isAuthenticated() && resource.data.userId == request.auth.uid
      );
      allow delete: if isOwner();
    }

    // --- STORE & GIFT CARDS ---
    match /admin_store_codes/{codeId} {
      allow read: if isAuthenticated();
      allow write: if isStaff() || isOwner();
    }

    match /store_settings/{itemId} {
      allow read: if isAuthenticated();
      allow create, delete: if isStaff() || isOwner();
      allow update: if isStaff() || isOwner() || (
        isAuthenticated() && 
        request.resource.data.diff(resource.data).affectedKeys().hasOnly(['codes'])
      );
    }

    match /store_items/{itemId} {
      allow read: if true;
      allow write: if isStaff();
    }

    // --- APP CONFIG, BANNERS & SETTINGS ---
    match /banners/{bannerId} {
      allow read: if true;
      allow write: if isStaff();
    }

    match /system_config/{configId} {
      allow read: if true;
      allow write: if isOwner() || isStaff();
    }

    match /app_config/{docId} {
      allow read: if true;
      allow write: if isStaff();
    }

    match /settings/{settingId} {
      allow read: if true;
      allow write: if isOwner() || isStaff();
    }

    // --- SUPPORT, FAQS & NOTIFICATIONS ---
    match /faqs/{faqId} {
      allow read: if isAuthenticated();
      allow write: if isStaff();
    }

    match /support_tickets/{ticketId} {
      allow read: if isAuthenticated() && (
        resource.data.userId == request.auth.uid || isStaff()
      );
      allow create: if isAuthenticated() && request.resource.data.userId == request.auth.uid;
      allow update: if isStaff() || (
        isAuthenticated() && resource.data.userId == request.auth.uid
      );
      allow delete: if isOwner();
    }

    match /notifications/{notifId} {
      allow read: if isAuthenticated();
      allow write: if isStaff();
    }

    match /global_notifications/{notifId} {
      allow read: if isAuthenticated();
      allow write: if isStaff();
    }

    match /broadcast_logs/{logId} {
      allow read, write: if isStaff();
    }

    match /broadcast_notifications/{notifId} {
      allow read: if true;
      allow write: if isStaff();
    }

    match /broadcasts/{broadcastId} {
      allow read: if true;
      allow write: if isStaff();
    }

    match /security_alerts/{alertId} {
      allow read: if isOwner();
      allow create: if isAuthenticated();
      allow update, delete: if isOwner();
    }

    match /security_incidents/{incidentId} {
      allow create: if isAuthenticated();
      allow read, update, delete: if isOwner();
    }

    // --- DEFAULT SECURE FALLBACK ---
    match /{document=**} {
      allow read: if isAuthenticated();
      allow write: if isOwner();
    }
  }
}
```
