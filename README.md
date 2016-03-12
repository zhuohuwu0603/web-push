# WebPush

A Web Push library for Java.

## Installation

For Gradle, add the following dependency:

```
compile group: 'nl.martijndwars', name: 'web-push', version: '0.1.0'
```

## Usage

```java
// Create a notification with the endpoint, userPublicKey from the subscription and a custom payload
Notification notification = new Notification(endpoint, userPublicKey, payload, ttl);

// Or create a GcmNotification, in case of Google Cloud Messaging, which does not support a payload/encryption
Notification notification = new GcmNotification(endpoint);

// Instantiate the push service with a GCM API key
PushService pushService = new PushService("4pik3y");

// Send the notification
pushService.send(notification);
```

## Credit

To give credit where credit is due, this library is mostly a Java port of marco-c/web-push. 

## Related

- For PHP, see [Minishlink/web-push](https://github.com/Minishlink/web-push)
- For nodejs, see [marco-c/web-push](https://github.com/marco-c/web-push) and [GoogleChrome/push-encryption-node](https://github.com/GoogleChrome/push-encryption-node)

