# KumuluzEE REST
[![Build Status](https://img.shields.io/travis/kumuluz/kumuluzee-rest/master.svg?style=flat)](https://travis-ci.org/kumuluz/kumuluzee-rest)

> KumuluzEE REST exposes your JPA entities to built-in metadata and query support using JAX-RS.

KumuluzEE REST provides automatic query parameter support when retrieving JPA entities using JAX-RS. It provides support for pagination, sorting and filtering of JPA entities, by parsing query parameters from the URI and using these query parameters when builing JPA query and retrieving the entities. 

## Usage

You can enable the KumuluzEE REST by adding the following dependency:
```xml
<dependency>
    <groupId>com.kumuluz.ee.rest</groupId>
    <artifactId>kumuluzee-rest-core</artifactId>
    <version>${kumuluzee-rest.version}</version>
</dependency>
```

### JAX-RS implementation

When implementing REST services the URI context information is needed. The URI can be obtained by adding UriInfo context to selected Resource:

```java
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("customers")
public class CustomerResource {

    @Context
    protected UriInfo uriInfo;
    
    ...
    
}
```

Using the URI context information the query parameters can be constructed by using the `QueryParameters` class:

```java
    @GET
    public Response getAllCustomers() {
        QueryParameters query = QueryParameters.query(uriInfo.getRequestUri().getQuery()).build();
        List<Customer> customers = customerBean.getCustomers(query);
        return Response.ok(customers).build();
    }

```

### CDI implementation

After parsing the query parameters they can be used to query or count entities using the `JPAUtils` class:


```java
@RequestScoped
public class CustomerService {

    @PersistenceContext
    private EntityManager em;

    public List<Customer> getCustomers(QueryParameters query) {
        List<Customer> customers = JPAUtils.queryEntities(em, Customer.class, query);
        return customers;
    }


    public Long getCustomerCount(QueryParameters query) {
        Long count = JPAUtils.queryEntitiesCount(em, Customer.class, query);
        return count;
    }
}
```

### Examples

After the implementation of Rest resources and CDI beans, the query parameters can be used for pagination, sorting and filtering of JPA entities.

#### Pagination

The offset parameter indicates the position of the first entity which should be returned and the limit parameter indicates the number of entities.

```
GET /v1/customers?offset=10
GET /v1/customers?limit=5
GET /v1/customers?offset=10&limit=5
```

#### Sorting

Sorting of entities can be specified by providing the field and direction.

```
GET v1/customers?order=id DESC
GET v1/customers?order=lastName ASC
```

#### Filtering

The entities can be filtered by using multiple operations:

* eq | Equals
* eqic | Equals ignore case
* neq | Not equal
* neqic | Not equal ignoring case
* like | Patern matching (% replaces characters)
* likeic | Patern matching ignore case (% replaces characters)
* gt | Greater than
* gte | Greater than or equal
* lt | Lower than
* lte | Lower than or equal
* in | In set
* inic | In set ignore case
* nin | Not in set
* ninic | Not in set ignore case
* isnull | Null
* isnotnull | Not null

```
GET v1/customers?filter=id:eq:1
GET v1/customers?filter=lastName:neqic:'doe'
GET v1/customers?filter=lastName:like:H%
GET v1/customers?filter=age:gt:10
GET v1/customers?filter=id:in:[1,2,3]
GET v1/customers?filter=lastName:isnull
GET v1/customers?filter=lastName:isnotnull

GET v1/customers?filter=age:gt:10 id:in:[1,2,3] lastName:isnotnull
```

## Changelog

Recent changes can be viewed on Github on the [Releases Page](https://github.com/kumuluz/kumuluzee-rest/releases)

## Contribute

See the [contributing docs](https://github.com/kumuluz/kumuluzee-rest/blob/master/CONTRIBUTING.md)

When submitting an issue, please follow the 
[guidelines](https://github.com/kumuluz/kumuluzee-rest/blob/master/CONTRIBUTING.md#bugs).

When submitting a bugfix, write a test that exposes the bug and fails before applying your fix. Submit the test 
alongside the fix.

When submitting a new feature, add tests that cover the feature.

## License

MIT
