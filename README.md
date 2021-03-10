# KumuluzEE REST
[![Build Status](https://travis-ci.org/kumuluz/kumuluzee-rest.svg?branch=master)](https://travis-ci.org/kumuluz/kumuluzee-rest)

> KumuluzEE REST greatly simplifies implementation of common REST patterns, such as paging, sorting and filtering. In conjunction with JPA it supports automated querying and retrieving of entities.

KumuluzEE REST provides support for common patterns and best practices for REST services using JAX-RS 2. It greatly simplifies the implementation of paging, sorting and filtering of REST resources and introduces a common syntax. It provides support for automatic parsing of query parameters. In conjunction with JPA it provides support for automated retrieving of entities, parsing query parameters from the URIs and using these query parameters when building JPA queries. 

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

When implementing REST services the URI context information is needed. The URI can be obtained by adding `UriInfo` context to selected Resource:

```java
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("customers")
public class CustomerResource {

    @Context
    protected UriInfo uriInfo;

    @Inject
    private CustomerService customerBean;
    
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
We can also build the query using `QueryStringDefaults` class which applies the following defaults (if not specified by the client):
- max results: maximum number of entities that can be returned
- limit: default number of entities returned
- offset: default offset

```java
@Context
private UriInfo uriInfo;

@Inject
private QueryStringDefaults qsd;

@Inject
private EntityManager em;

@GET
public Response getList() {
    QueryParameters query = qsd.builder().queryEncoded(uriInfo.getRequestUri().getRawQuery()).build();

    List<Customer> allCustomers = JPAUtils.queryEntities(em, Customer.class, query);
    Long allCustomersCount = JPAUtils.queryEntitiesCount(em, Customer.class, query);

    return Response.ok(allCustomers).header("X-Total-Count", allCustomersCount).build();
}
```
Defaults can either be constructed:
```java
private QueryStringDefaults qsd = new QueryStringDefaults().maxLimit(100).defaultLimit(20).defaultOffset(0);
```
or injected with CDI and a producer class:
```java
public class RestProducer {

    @Produces
    @ApplicationScoped
    public QueryStringDefaults getQueryStringDefaults() {
        return new QueryStringDefaults()
                .maxLimit(100)
                .defaultLimit(20)
                .defaultOffset(0);
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
We must also return the number of all entities so the client can display correct number of page buttons. One way of doing this is with a custom HTTP header.
```java
...
QueryParameters query = qsd.builder().queryEncoded(uriInfo.getRequestUri().getRawQuery()).build();

List<Customer> allCustomers = JPAUtils.queryEntities(em, Customer.class, query);
Long allCustomersCount = JPAUtils.queryEntitiesCount(em, Customer.class, query);

return Response.ok(allCustomers).header("X-Total-Count", allCustomersCount).build();
```

With very large datasets counting all filtered records can be very slow. That is why it's possible to specify the `count` parameter, which can than be used to decide whether or not to perform the count.

```
GET /v1/customers?offset=10
GET /v1/customers?count=true&limit=5
GET /v1/customers?count=false&offset=10&limit=5
```

Parameter `count` is set to `true`by default.

__NOTE__: When using `JpaUtils.getQueried` counting is performed (or not performed) automatically, depending on the value of `count`.

#### Sorting

Sorting of entities can be specified by providing the field and direction.

```
GET v1/customers?order=id DESC
GET v1/customers?order=lastName ASC
```
We can chain several sorts together.
```
GET v1/customers?order=email ASC,lastname DESC
```
After the last user specified sort, order by unique ID is automatically appended at the end of the query for deterministic sorting of same-valued columns.

#### Filtering

The entities can be filtered by using multiple operations:

* EQ | Equals
* EQIC | Case-insensitive equals
* NEQ | Not equal
* NEQIC | Case-insensitive not equal
* LIKE | Pattern matching (% replaces characters, _ replaces a single character)
* LIKEIC | Case-insensitive pattern matching (% replaces characters, _ replaces a single character)
* NLIKE | Negated pattern matching (% replaces characters, _ replaces a single character)
* NLIKEIC | Case-insensitive negated pattern matching (% replaces characters, _ replaces a single character)
* GT | Greater than
* GTE | Greater than or equal
* LT | Lower than
* LTE | Lower than or equal
* IN | In set
* INIC | Case-insensitive in set
* NIN | Not in set
* NINIC | Case-insensitive not in set
* ISNULL | Null
* ISNOTNULL | Not null

```
GET v1/customers?filter=id:EQ:1
GET v1/customers?filter=lastName:NEQIC:'doe'
GET v1/customers?filter=lastName:LIKE:H%
GET v1/customers?filter=age:GT:10
GET v1/customers?filter=id:IN:[1,2,3]
GET v1/customers?filter=lastName:ISNULL
GET v1/customers?filter=lastName:ISNOTNULL

GET v1/customers?filter=age:GT:10 id:IN:[1,2,3] lastName:ISNOTNULL
```

By default, filters are chained together with an `AND` operator (represented by an empty space). 

#### Complex queries
It is possible to write more complex queries by using `OR` and `AND` operators and by grouping them together with 
__parentheses__. Both `OR` and `AND` operator can be written in several different ways:
* OR | `,`, `or`
* AND | ` `, `;`, `and`

__NOTE__: `AND` has precedence over `OR`, __parantheses__ have precedence over `AND`

```
GET v1/customers?filter=age:GT:10 id:IN:[1,2,3] lastName:ISNOTNULL, firstName:LIKE:'B%'
GET v1/customers?filter=age:GT:10 id:IN:[1,2,3] and lastName:ISNOTNULL or firstName:LIKE:'B%'
GET v1/customers?filter=age:GT:10 id:IN:[1,2,3]; lastName:ISNOTNULL, firstName:LIKE:'B%'
GET v1/customers?filter=age:GT:10 id:IN:[1,2,3] and (lastName:ISNOTNULL or firstName:LIKE:'B%')
```

There are some special cases:
- If we want to use `LIKE` filter and query values that include a percent sign, it needs to be URL encoded (%25).
- Dates and instants must be in ISO-8601 format, `+` sign must be URL encoded (%2B). Single quotes for value are required.
```
GET v1/customers?where=firstName:LIKE:'%somestring%25doe'
GET v1/customers?where=createdAt:GT:'2017-06-12T11:57:00%2B00:00'
```

#### Partial responses
We can select which fields we want returned in the resulting JSON with the `fields` parameter.
```
GET v1/customers?fields=firstName,lastName
```

#### Traversing OneToMany and ManyToOne relations
We can traverse entity attributes similar to JPQL style. Let's say each customer has many `cars` and we want to find owners of specific brand:
```
GET v1/customers?filter=cars.brand:EQ:bmw
```
It also works in reverse:
```
GET v1/cars?filter=customer.firstName:EQ:John
```
This would find all `Cars` that have an owner named `John`.

#### Combine pagination, sorting and filtering

Pagination, sorting and filtering of entities can be combined by separating them with &.

```
GET /v1/customers?offset=10&limit=5&order=id DESC&filter=age:GT:10 id:IN:[1,2,3] lastName:ISNOTNULL
```

#### Custom field mapping
Library supports custom property mappings set on entity properties in order to detach API schema from database model. This functionality turns out to be useful when persistence changes are needed and API needs to stay backwards compatible.
```java
@RestMapping("experience")
private Integer years;
```
In this case both of the following queries will return same result:
```
GET /v1/customers?filter=years:EQ:5
GET /v1/customers?filter=experience:EQ:5
```
Library supports combination of properties with child properties using OneToOne mapping:
```java
@RestMapping("emailAndCurrentPosition")
private String email;

@RestMapping(value = "emailAndCurrentPosition", toChildField = "currentPosition")
@OneToOne(mappedBy = "user")
private UserCareer career;
```
where result will return only email and current position within career relation.

#### Ignoring REST fields
By default, REST library returns field not found exception for non-existing entity fields. For some cases we may want to omit field check with annotation:
 ```java
@Entity
@RestIgnore("userIgnoredField")
@Table(name = "users")
public class User {
    ...
}
 ```


#### Additional criteria query manipulation
Predicate constructed from query parameters can be further changed. For example:

```java
List<Customer> allCustomers = JPAUtils.queryEntities(em, Customer.class,
(p, cb, r) -> cb.and(p, cb.equal(r.get("firstName"), "John")));
```
Where:
- `p` is existing Predicate
- `cb` is CriteriaBuilder and
- `r` is Root 

With this, programmer has the full power of Criteria API to further manipulate the query.

## Building

Ensure you have JDK 8 (or newer), Maven 3.2.1 (or newer) and Git installed

```bash
    java -version
    mvn -version
    git --version
```

First clone the repository:

```bash
    git clone https://github.com/kumuluz/kumuluzee-rest.git
    cd kumuluzee-rest
```
    
To build run:

```bash
    mvn install
```

This will build all modules and run the testsuite. 
    
Once completed you will find the build archives in the modules respected `target` folder and local `.m2` repository.

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
