QCM & Réponses — Spring / Spring Boot (synthèse courte)
1) @Component vs @Service vs @Repository

@Component : stéréotype générique pour tout bean Spring.

@Service : même chose mais pour la couche métier (lisibilité, intention).

@Repository : couche persistance ; ajoute la traduction d’exceptions en DataAccessException.

Quand ?

Accès DB → @Repository

Règles métier / orchestration → @Service

Utilitaires/adaptateurs → @Component

2) Injection de dépendances (DI)

Spring instancie et fournit tes dépendances (au lieu de new).
Bénéfices : découplage, testabilité (mocks), remplacement aisé, config centralisée.

✓ Recommandé : injection par constructeur (dépendances obligatoires, immuables).

3) Spring Boot simplifie la configuration

Auto-configuration (selon le classpath : Web, JPA, Security…).

Starters (dépendances prêtes à l’emploi).

Convention over configuration (valeurs par défaut sensées).

Actuator (observabilité, santé, métriques).

4) @RestController vs @Controller

@Controller : renvoie des vues (templates). Pour renvoyer du JSON, il faut @ResponseBody sur la méthode.

@RestController : équivaut à @Controller + @ResponseBody sur toutes les méthodes → idéal pour APIs REST (JSON).

5) @Autowired et types d’injection

@Autowired demande l’injection d’un bean.

Par constructeur (préférée) : claire, testable, sûre.

Par champ : pratique, mais moins testable.

Par setter : utile pour dépendances optionnelles.

6) Spring Data JPA & CRUD

Déclare une interface qui étend JpaRepository<T, ID> : CRUD déjà fourni.

Méthodes dérivées du nom (ex. findByNameContainingIgnoreCase).

Exemple rapide :

public interface ProductRepository extends JpaRepository<Product, Long> {}

7) Transactions

@Transactional gère ouverture/commit/rollback.

Par défaut rollback sur RuntimeException.

Place l’annotation en couche service (unité métier).

Options : propagation, isolation, timeout, readOnly.

8) @Bean en configuration

Dans une classe @Configuration, une méthode @Bean déclare un bean dans le contexte Spring (utile pour objets de bibliothèques externes, clients HTTP, mappers, etc.).

9) Spring Security (avantages & basic auth)

Avantages : filtre standardisé, authN/authZ, CSRF, method security (@PreAuthorize), OAuth2…

Basic auth : configuration minimale via SecurityFilterChain (DSL moderne), utilisateurs en mémoire pour démarrer.

10) application.properties / application.yml

Fichiers de configuration externe : port, datasource, logs, profils, etc.
Exemple (YAML) :

server:
  port: 8081
spring:
  datasource:
    url: jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1
  jpa:
    hibernate:
      ddl-auto: update
logging:
  level:
    root: INFO

11) Profils (@Profile)

Permettent des configs/beans par environnement (dev, test, prod).
Activer : spring.profiles.active=dev (prop, env, argument JVM).
Fichiers dédiés : application-dev.yml, application-prod.yml, etc.

12) Tests d’intégration REST avec @SpringBootTest

Approche standard :

@SpringBootTest pour démarrer le contexte,

@AutoConfigureMockMvc pour injecter MockMvc,

Assertions sur le statut et le JSON.

Alternative légère : MockMvc “standalone” sans démarrer Spring (instancier le contrôleur + mocks).

Annexes — Mini-exemples
A) Service transactionnel
@Service
public class BankService {
  private final AccountRepository repo;
  public BankService(AccountRepository repo) { this.repo = repo; }

  @Transactional
  public void transfer(Long from, Long to, BigDecimal amount) {
    var a = repo.getReferenceById(from);
    var b = repo.getReferenceById(to);
    a.debit(amount);
    b.credit(amount);
  }
}

B) Contrôleur REST simple
@RestController
@RequestMapping("/api/products")
class ProductController {
  private final ProductService service;
  public ProductController(ProductService service) { this.service = service; }

  @GetMapping("/{id}")
  ProductDto get(@PathVariable Long id) { return service.get(id); }

  @PostMapping
  ResponseEntity<ProductDto> create(@RequestBody ProductDto dto) {
    var saved = service.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }
}

C) Test d’intégration REST (version “standalone”)
class ProductApiTest {
  private MockMvc mvc;

  @BeforeEach
  void setup() {
    var controller = new ProductController(/* mocks/service */);
    mvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void create() throws Exception {
    mvc.perform(post("/api/products")
         .contentType(MediaType.APPLICATION_JSON)
         .content("""{"name":"Keyboard"}"""))
      .andExpect(status().isCreated());
  }
}