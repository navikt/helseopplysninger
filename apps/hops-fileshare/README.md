# HOPS Fileshare

## Context

In many healthcare workflows it is common to reference to attachment files, these could be images, pdfs, videos, or some other formats. These files should be available as long as the resources referencing it exists.

## Solution

This service provides a simple API to upload and download files, it uses Google Cloud Storage to store the files. 
It is also uncertain what size these files can be, so it is better to not rely on the local hard drive to temporarily store the file, this is why this service is purposely only streaming the files that are uploaded into it, that is why we cannot use the Google sdk as it relies on the java file API to work.

To keep the service simple and avoid extra dependencies we intended to use the metadata attached to the Google Cloud Storage object to store information about who stored the file and who can view it. However this is not yet implemented.

For security concerns this service uses [ClamAV](https://doc.nais.io/security/antivirus/) to scan the uploaded files before storing them in the permanent store.

It is also desired that calls to this service are idempotent, so we first transfer the files to a "unscanned" bucket and use its hash to see if we already have the file in the permanent bucket. The upload flow is at follows:

[![](https://mermaid.ink/img/eyJjb2RlIjoic2VxdWVuY2VEaWFncmFtXG4gICAgcGFydGljaXBhbnQgQSBhcyBVc2VyXG4gICAgcGFydGljaXBhbnQgRCBhcyBob3BzLWZpbGVzaGFyZVxuICAgIHBhcnRpY2lwYW50IEMgYXMgdW5zY2FubmVkLWJ1Y2tldFxuICAgIHBhcnRpY2lwYW50IFYgYXMgQ2xhbUFWXG4gICAgcGFydGljaXBhbnQgSyBhcyBwZXJtYW5lbnQtYnVja2V0XG4gICAgQS0-PkQ6IFVwbG9hZCBmaWxlXG4gICAgYWN0aXZhdGUgRFxuICAgIEQtPj5DOiBUcmFuc2ZlciBmaWxlXG4gICAgQy0-PkQ6IEZpbGUgbWV0YWRhdGEgKGhhc2gpXG4gICAgRC0-PlY6IFNjYW5cbiAgICBhbHQgU2NhbiByZXN1bHRcbiAgICAgICAgVi0-PkQ6IE9LXG4gICAgZWxzZVxuICAgICAgICBWLT4-RDogTm90IE9LXG4gICAgICAgIEQtPj5BOiBFcnJvclxuICAgIGVuZFxuICAgIFxuICAgIEQtPj5LOiBUcmFuc2ZlciBmaWxlXG4gICAgRC0-PkE6IEZpbGUgbWV0YWRhdGEgKyBJRFxuICAgIGRlYWN0aXZhdGUgRFxuICAiLCJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJ1cGRhdGVFZGl0b3IiOmZhbHNlLCJhdXRvU3luYyI6dHJ1ZSwidXBkYXRlRGlhZ3JhbSI6ZmFsc2V9)](https://mermaid-js.github.io/mermaid-live-editor/edit/#eyJjb2RlIjoic2VxdWVuY2VEaWFncmFtXG4gICAgcGFydGljaXBhbnQgQSBhcyBVc2VyXG4gICAgcGFydGljaXBhbnQgRCBhcyBob3BzLWZpbGVzaGFyZVxuICAgIHBhcnRpY2lwYW50IEMgYXMgdW5zY2FubmVkLWJ1Y2tldFxuICAgIHBhcnRpY2lwYW50IFYgYXMgQ2xhbUFWXG4gICAgcGFydGljaXBhbnQgSyBhcyBwZXJtYW5lbnQtYnVja2V0XG4gICAgQS0-PkQ6IFVwbG9hZCBmaWxlXG4gICAgYWN0aXZhdGUgRFxuICAgIEQtPj5DOiBUcmFuc2ZlciBmaWxlXG4gICAgQy0-PkQ6IEZpbGUgbWV0YWRhdGEgKGhhc2gpXG4gICAgRC0-PlY6IFNjYW5cbiAgICBhbHQgU2NhbiByZXN1bHRcbiAgICAgICAgVi0-PkQ6IE9LXG4gICAgZWxzZVxuICAgICAgICBWLT4-RDogTm90IE9LXG4gICAgICAgIEQtPj5BOiBFcnJvclxuICAgIGVuZFxuICAgIFxuICAgIEQtPj5LOiBUcmFuc2ZlciBmaWxlXG4gICAgRC0-PkE6IEZpbGUgbWV0YWRhdGEgKyBJRFxuICAgIGRlYWN0aXZhdGUgRFxuICAiLCJtZXJtYWlkIjoie1xuICBcInRoZW1lXCI6IFwiZGVmYXVsdFwiXG59IiwidXBkYXRlRWRpdG9yIjpmYWxzZSwiYXV0b1N5bmMiOnRydWUsInVwZGF0ZURpYWdyYW0iOmZhbHNlfQ)


