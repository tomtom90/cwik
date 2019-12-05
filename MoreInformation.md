## Motivation

In short this project is supposed to give an interactive testsuite for the transport protocol 
standard quic currently discussed by the IETF and introduced by Google.
The goal is to give a tool that can open connections and test them with some basic instruments, such has HTTP 0.9
requests, that later can be modified and extended to suit the needs of the user.

## Design

The project is aimed to offer the basic functionality of quic so that it can be expanded later and become more power- 
and useful. The vital function is therefor the ```connectHost``` as it returns either the QuicConnection
as object or a vector holding error information. Regardless of what you intend to do, this connection object is the key peace.

## Outlook

This is a student project, the continuation of it is therefore not explicitly given. However it is a basic tool and can
be expanded and might become a good tool for tests on quic connection.

Additional features could be some more detailed access to the connection parameters
such as ID's and keys. An adoption of the kwik library would be needed though.
As protocols (HTTP3, DNS/QUIC, etc.) for quic are implemented they can be used t extend this framework as well.

## Adaptation of Kwik
In order to use the Kwik library some number of member access rights had to be changed. As Kwik matured those changes
could be reduced to a single class variable and the removal of a function from the gradle build file.
Due to those changes commit 5a80a6b is the lowest supported version of Kwik.
