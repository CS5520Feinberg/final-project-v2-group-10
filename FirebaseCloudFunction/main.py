# Welcome to Cloud Functions for Firebase for Python!
# To get started, simply uncomment the below code or create your own.
# Deploy with `firebase deploy`

from firebase_functions.firestore_fn import (
  on_document_created,
  on_document_deleted,
  on_document_updated,
  on_document_written,
  Event,
  Change,
  DocumentSnapshot,
)

from firebase_functions import https_fn
from firebase_admin import initialize_app, firestore, messaging
import google.cloud.firestore

initialize_app()
#
#
# @https_fn.on_request()
# def on_request_example(req: https_fn.Request) -> https_fn.Response:
#     return https_fn.Response("Hello world!")

# Notifications sent by client:
# fields: type: 0. message 1. post reply
# for post:   postType, postId, postTitle, userName,

@on_document_created(document="users/{userId}/notifications/{notificationId}")
def notify(event: Event[DocumentSnapshot]) -> None:
  if event.data is None:
    return
  receiver = event.params["userId"]
  notifyId = event.params["notificationId"]
  try:
    type = event.data.to_dict()["type"]
  except KeyError:
    return
  if type == 1:
    firestore_client: google.cloud.firestore.Client = firestore.client()
    try: 
      token = firestore_client.document("users/" + receiver).get().to_dict()["token"]
    except KeyError:
      return
    record = event.data.to_dict()
    title = record["userName"] + " reply to you:"
    body =  record["text"] + " -post- " + record["postTitle"]
    data = {"postType": record["postType"], "postId":record["postId"], "notifyId":notifyId}
    message = messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=body,
            ),
            data=data,
            token=token
        )
    messaging.send(message)
    




