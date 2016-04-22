# Frequently Asked Questions #

**Why use async callbacks instead of just blocking?**
> This is required for GWT compatibility. It would be feasible to define a separate blocking API,
> but one gets used to the async style quickly.

**Why is a proxy required for GWT**
  * The GWT Jsonp implementation generates Jsonp callbacks starting with an underscore. The fusion table API does not accept Jsonp callback names starting with underscores.
  * The Fusion Table API does not permit GET request for modifying commands such as _update_ and _insert_. Jsonp only works with GET requests.
  * There is no Jsonp interface for OAuth 1.0 available at Google
