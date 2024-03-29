# HOPS Fileshare

## Context

In many healthcare workflows it is common to reference to attachment files, these could be images, pdfs, videos, or some other formats. These files should be available as long as the resources referencing it exists.

## Solution

This service provides a simple API to upload and download files, it uses Google Cloud Storage to store the files. 
It is also uncertain what size these files can be, so it is better to not rely on the local hard drive to temporarily store the file, this is why this service is purposely only streaming the files that are uploaded into it, that is why we cannot use the Google sdk as it relies on the java file API to work.

To keep the service simple and avoid extra dependencies we intended to use the metadata attached to the Google Cloud Storage object to store information about who stored the file and who can view it. However this is not yet implemented.

For security concerns this service uses [ClamAV](https://doc.nais.io/security/antivirus/) to scan the uploaded files before storing them in the permanent store.

It is also desired that calls to this service are idempotent, so we first transfer the files to a "unscanned" bucket and use its hash to see if we already have the file in the permanent bucket. The upload flow is at follows:

[![](https://mermaid.ink/img/eyJjb2RlIjoic2VxdWVuY2VEaWFncmFtXG4gICAgcGFydGljaXBhbnQgQSBhcyBDbGVpbnRcbiAgICBwYXJ0aWNpcGFudCBEIGFzIGhvcHMtZmlsZXNoYXJlXG4gICAgcGFydGljaXBhbnQgQyBhcyB1bnNjYW5uZWQtYnVja2V0XG4gICAgTm90ZSBvdmVyIEM6IFRlcm1wb3JhcnkgcmV0ZW50aW9uIHBvbGljeVxuICAgIHBhcnRpY2lwYW50IFYgYXMgQ2xhbUFWXG4gICAgcGFydGljaXBhbnQgSyBhcyBwZXJtYW5lbnQtYnVja2V0XG4gICAgTm90ZSBvdmVyIEs6IFBlcm1hbmVudCByZXRlbnRpb24gcG9saWN5XG4gICAgQS0-PkQ6IFVwbG9hZCBmaWxlXG4gICAgYWN0aXZhdGUgRFxuICAgIEQtPj5DOiBUcmFuc2ZlciBmaWxlXG4gICAgQy0-PkQ6IEZpbGUgbWV0YWRhdGEgKGhhc2gpXG4gICAgRC0-Pks6IFF1ZXJ5KGhhc2gpXG4gICAgYWx0IFF1ZXJ5IHJlc3VsdFxuICAgICAgICBLLT4-RDogRm91bmRcbiAgICAgICAgRC0-PkE6IEZpbGUgbWV0YWRhdGFcbiAgICBlbHNlXG4gICAgICAgIEQtPj5WOiBTY2FuXG4gICAgICAgIFYtPj5EOiBPSyAgICBcbiAgICAgICAgRC0-Pks6IFRyYW5zZmVyIGZpbGVcbiAgICAgICAgRC0-PkE6IEZpbGUgbWV0YWRhdGFcbiAgICBlbmRcbiAgICBkZWFjdGl2YXRlIERcbiAgIiwibWVybWFpZCI6eyJ0aGVtZSI6ImRlZmF1bHQifSwidXBkYXRlRWRpdG9yIjpmYWxzZSwiYXV0b1N5bmMiOnRydWUsInVwZGF0ZURpYWdyYW0iOmZhbHNlfQ)](https://mermaid-js.github.io/mermaid-live-editor/edit/#eyJjb2RlIjoic2VxdWVuY2VEaWFncmFtXG4gICAgcGFydGljaXBhbnQgQSBhcyBDbGVpbnRcbiAgICBwYXJ0aWNpcGFudCBEIGFzIGhvcHMtZmlsZXNoYXJlXG4gICAgcGFydGljaXBhbnQgQyBhcyB1bnNjYW5uZWQtYnVja2V0XG4gICAgTm90ZSBvdmVyIEM6IFRlcm1wb3JhcnkgcmV0ZW50aW9uIHBvbGljeVxuICAgIHBhcnRpY2lwYW50IFYgYXMgQ2xhbUFWXG4gICAgcGFydGljaXBhbnQgSyBhcyBwZXJtYW5lbnQtYnVja2V0XG4gICAgTm90ZSBvdmVyIEs6IFBlcm1hbmVudCByZXRlbnRpb24gcG9saWN5XG4gICAgQS0-PkQ6IFVwbG9hZCBmaWxlXG4gICAgYWN0aXZhdGUgRFxuICAgIEQtPj5DOiBUcmFuc2ZlciBmaWxlXG4gICAgQy0-PkQ6IEZpbGUgbWV0YWRhdGEgKGhhc2gpXG4gICAgRC0-Pks6IFF1ZXJ5KGhhc2gpXG4gICAgYWx0IFF1ZXJ5IHJlc3VsdFxuICAgICAgICBLLT4-RDogRm91bmRcbiAgICAgICAgRC0-PkE6IEZpbGUgbWV0YWRhdGFcbiAgICBlbHNlXG4gICAgICAgIEQtPj5WOiBTY2FuXG4gICAgICAgIFYtPj5EOiBPSyAgICBcbiAgICAgICAgRC0-Pks6IFRyYW5zZmVyIGZpbGVcbiAgICAgICAgRC0-PkE6IEZpbGUgbWV0YWRhdGFcbiAgICBlbmRcbiAgICBkZWFjdGl2YXRlIERcbiAgIiwibWVybWFpZCI6IntcbiAgXCJ0aGVtZVwiOiBcImRlZmF1bHRcIlxufSIsInVwZGF0ZUVkaXRvciI6ZmFsc2UsImF1dG9TeW5jIjp0cnVlLCJ1cGRhdGVEaWFncmFtIjpmYWxzZX0)


