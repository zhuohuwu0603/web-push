# WebPush

A Web Push library for Java.

## Installation

For Gradle, add the following dependency to `build.gradle`:

```
compile group: 'nl.martijndwars', name: 'web-push', version: '1.0.0'
```

For Maven, add the following dependency to `pom.xml`:

```
<dependency>
    <groupId>nl.martijndwars</groupId>
    <artifactId>web-push</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

The server should have stored a subscription containing the `userPublicKey` and `userAuth` keys. Use these keys to create a `Notification` or `GcmNotification`, depending on whether the subscription is for Google Cloud Messaging.

```java
// Create a notification with the endpoint, userPublicKey from the subscription and a custom payload
Notification notification = new Notification(endpoint, userPublicKey, userAuth, payload, ttl);

// Or create a GcmNotification, in case of Google Cloud Messaging
Notification notification = new GcmNotification(endpointi, userPublicKey, userAuth, payload);

// Instantiate the push service with a GCM API key
PushService pushService = new PushService("gcm-api-key");

// Send the notification
pushService.send(notification);
```

## Credit

To give credit where credit is due, the PushService is mostly a Java port of marco-c/web-push. The HttpEce class is mostly a Java port of martinthomson/encrypted-content-encoding.

## Related

- For PHP, see [Minishlink/web-push](https://github.com/Minishlink/web-push)
- For nodejs, see [marco-c/web-push](https://github.com/marco-c/web-push) and [GoogleChrome/push-encryption-node](https://github.com/GoogleChrome/push-encryption-node)
- For python, see [mozilla-services/pywebpush](https://github.com/mozilla-services/pywebpush)
