# Agent

This document is still a WIP
The agent is the android app itself which runs on the device. The agent is in charge of making the various calls to the jsolar library to begin with.

## Activities

These are just android activities... Quick check doesnt look like they implement any "functionality", just providing the screens

**StartBroadcastActivity** appears to be some mess made by Ken before he left, who just pushes "yayintentyay" to master???
This might have been for testing? unsure at least, will probably be safe to delete before we go public with a new version

## Helpers

**IntentProxyToContentProvider**: Code is actually documented, this feels however it should have just been implemented as a drozer module and not as an activity?
Again some more Ken messy code.

## Models

**EndpointManager**: Appears to provide main logic for managing android Interprocess Communication Endpoints, and more specifically for saving them in the internal SQL DB.

**ServerSettings**: Simple preference editor / getter

## Providers

**Provider**: Good question as to what this does, seems to be another Ken feature

## Receivers

**StartMainActivityReceiver**: 

**StartServiceReceiver**: 

## Service Connectors

## Services

## Views

