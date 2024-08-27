public class CrptApi {

    private static final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Semaphore semaphore;
    private final ScheduledExecutorService scheduler;

    public CrptApi(TimeUnit timeUnit, int requestLimit){
        this.httpClient = HttpClient.newBuilder().build();
        this.objectMapper = new ObjectMapper();
        this.semaphore = new Semaphore(requestLimit);
        this.scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(semaphore::release, 0,1,timeUnit);
    }

    public void createDocument(Document document) throws InterruptedException, IOException {
        semaphore.acquire();

        String jsonBody = objectMapper.writeValueAsString(document);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("Document created successfully: " + response.body());
        } else {
            System.err.println("Error while creating document: " + response.body());
        }
    }

    public static class Document{
        public Description description;
        public String doc_id;
        public String doc_status;
        public String doc_type = "LP_INTRODUCE_GOODS";
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public Product[] products;
        public String reg_date;
        public String reg_number;
    }

    public static class Description{
        public String participantInn;
    }

    public static class Product{
        public String certificate_document;
        public String certificate_document_date;
        public String certificate_document_number;
        public String owner_inn;
        public String producer_inn;
        public String production_date;
        public String tnved_code;
        public String uit_code;
        public String uitu_code;
    }
    public void shutdown() {
        scheduler.shutdown();
    }
}
