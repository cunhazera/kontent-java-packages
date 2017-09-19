/*
 * MIT License
 *
 * Copyright (c) 2017
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.kenticocloud.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.apache.http.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.localserver.LocalServerTestBase;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveryClientTest extends LocalServerTestBase {

    @Test
    public void testGetItems() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "items"),
                (request, response, context) -> {
                    String uri = String.format("http://testserver%s", request.getRequestLine().getUri());

                    List<NameValuePair> nameValuePairs =
                            URLEncodedUtils.parse(URI.create(uri), Charset.defaultCharset());
                    Map<String, String> params = convertNameValuePairsToMap(nameValuePairs);
                    Assert.assertEquals(1, params.size());
                    Assert.assertEquals("/path1/path2/test-article",params.get("elements.url_pattern"));
                    response.setEntity(
                            new InputStreamEntity(
                                    this.getClass().getResourceAsStream("SampleContentItemList.json")
                            )
                    );
                });
        HttpHost httpHost = this.start();
        String testServerUri = httpHost.toURI() + "/%s";
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setProjectId(projectId);
        deliveryOptions.setProductionEndpoint(testServerUri);

        DeliveryClient client = new DeliveryClient(deliveryOptions);
        client.setContentLinkUrlResolver(Link::getUrlSlug);
        client.setBrokenLinkUrlResolver(() -> "/404");

        List<NameValuePair> urlPattern = DeliveryParameterBuilder.params().filterEquals("elements.url_pattern", "/path1/path2/test-article").build();
        ContentItemsListingResponse items = client.getItems(urlPattern);

        Assert.assertNotNull(items);
        Assert.assertTrue(((RichTextElement) items.getItems().get(1).getElements().get("description")).getValue().contains("href=\"/on roasts\""));
    }

    @Test
    public void testGetAllItems() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "items"),
                (request, response, context) -> response.setEntity(
                        new InputStreamEntity(
                                this.getClass().getResourceAsStream("SampleContentItemList.json")
                        )
                ));
        HttpHost httpHost = this.start();
        DeliveryClient client = new DeliveryClient(projectId);

        //modify default baseurl to point to test server, this is private so using reflection
        String testServerUri = httpHost.toURI() + "/%s";
        Field deliveryOptionsField = client.getClass().getDeclaredField("deliveryOptions");
        deliveryOptionsField.setAccessible(true);
        ((DeliveryOptions) deliveryOptionsField.get(client)).setProductionEndpoint(testServerUri);

        ContentItemsListingResponse items = client.getItems();
        Assert.assertNotNull(items);
    }

    @Test
    public void testGetItemWithParams() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "items/on_roasts"),
                (request, response, context) -> {
                    String uri = String.format("http://testserver%s", request.getRequestLine().getUri());

                    List<NameValuePair> nameValuePairs =
                            URLEncodedUtils.parse(URI.create(uri), Charset.defaultCharset());
                    Map<String, String> params = convertNameValuePairsToMap(nameValuePairs);
                    Assert.assertEquals(1, params.size());
                    Assert.assertEquals("/path1/path2/test-article",params.get("elements.url_pattern"));
                    response.setEntity(
                            new InputStreamEntity(
                                    this.getClass().getResourceAsStream("SampleContentItem.json")
                            )
                    );
                });
        HttpHost httpHost = this.start();
        String testServerUri = httpHost.toURI() + "/%s";
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setProjectId(projectId);
        deliveryOptions.setProductionEndpoint(testServerUri);

        DeliveryClient client = new DeliveryClient(deliveryOptions);
        client.setContentLinkUrlResolver(Link::getUrlSlug);

        List<NameValuePair> urlPattern = DeliveryParameterBuilder.params().filterEquals("elements.url_pattern", "/path1/path2/test-article").build();
        ContentItemResponse item = client.getItem("on_roasts", urlPattern);

        Assert.assertNotNull(item);
    }

    @Test
    public void testGetItem() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "items/on_roasts"),
                (request, response, context) -> response.setEntity(
                        new InputStreamEntity(
                                this.getClass().getResourceAsStream("SampleContentItem.json")
                        )
                ));
        HttpHost httpHost = this.start();
        DeliveryClient client = new DeliveryClient(projectId);

        //modify default baseurl to point to test server, this is private so using reflection
        String testServerUri = httpHost.toURI() + "/%s";
        Field deliveryOptionsField = client.getClass().getDeclaredField("deliveryOptions");
        deliveryOptionsField.setAccessible(true);
        ((DeliveryOptions) deliveryOptionsField.get(client)).setProductionEndpoint(testServerUri);

        ContentItemResponse item = client.getItem("on_roasts");
        Assert.assertNotNull(item);
    }

    @Test
    public void testGetTaxonomyList() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "taxonomies"),
                (request, response, context) -> response.setEntity(
                        new InputStreamEntity(
                                this.getClass().getResourceAsStream("SampleTaxonomyGroupListingResponse.json")
                        )
                ));
        HttpHost httpHost = this.start();
        DeliveryClient client = new DeliveryClient(projectId);

        //modify default baseurl to point to test server, this is private so using reflection
        String testServerUri = httpHost.toURI() + "/%s";
        Field deliveryOptionsField = client.getClass().getDeclaredField("deliveryOptions");
        deliveryOptionsField.setAccessible(true);
        ((DeliveryOptions) deliveryOptionsField.get(client)).setProductionEndpoint(testServerUri);

        TaxonomyGroupListingResponse response = client.getTaxonomyGroups();
        Assert.assertNotNull(response);
    }

    @Test
    public void testGetTaxonomyGroup() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "taxonomies/personas"),
                (request, response, context) -> response.setEntity(
                        new InputStreamEntity(
                                this.getClass().getResourceAsStream("SampleTaxonomyGroup.json")
                        )
                ));
        HttpHost httpHost = this.start();
        DeliveryClient client = new DeliveryClient(projectId);

        //modify default baseurl to point to test server, this is private so using reflection
        String testServerUri = httpHost.toURI() + "/%s";
        Field deliveryOptionsField = client.getClass().getDeclaredField("deliveryOptions");
        deliveryOptionsField.setAccessible(true);
        ((DeliveryOptions) deliveryOptionsField.get(client)).setProductionEndpoint(testServerUri);

        TaxonomyGroup taxonomyGroup = client.getTaxonomyGroup("personas");
        Assert.assertNotNull(taxonomyGroup);
    }

    @Test
    public void testCache() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";
        DeliveryClient client = new DeliveryClient(projectId);
        final boolean[] cacheHit = {false};
        client.setCacheManager(new CacheManager() {
            @Override
            public JsonNode resolveRequest(String requestUri, HttpRequestExecutor executor) throws IOException {
                Assert.assertEquals("https://deliver.kenticocloud.com/02a70003-e864-464e-b62c-e0ede97deb8c/items/on_roasts", requestUri);
                cacheHit[0] = true;
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JSR310Module());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                return objectMapper.readValue(this.getClass().getResourceAsStream("SampleContentItem.json"), JsonNode.class);
            }
        });
        ContentItemResponse item = client.getItem("on_roasts");
        Assert.assertNotNull(item);
        Assert.assertTrue(cacheHit[0]);
    }

    @Test
    public void testGetStronglyTypedItem() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "items/on_roasts"),
                (request, response, context) -> response.setEntity(
                        new InputStreamEntity(
                                this.getClass().getResourceAsStream("SampleContentItem.json")
                        )
                ));
        HttpHost httpHost = this.start();
        DeliveryClient client = new DeliveryClient(projectId);

        //modify default baseurl to point to test server, this is private so using reflection
        String testServerUri = httpHost.toURI() + "/%s";
        Field deliveryOptionsField = client.getClass().getDeclaredField("deliveryOptions");
        deliveryOptionsField.setAccessible(true);
        ((DeliveryOptions) deliveryOptionsField.get(client)).setProductionEndpoint(testServerUri);

        ArticleItem item = client.getItem("on_roasts", ArticleItem.class);
        Assert.assertNotNull(item);
        Assert.assertNotNull(item.getSystemInformationObject());
        Assert.assertNull(item.getRandomValue());
        Assert.assertNull(item.getRandomStringList());
        Assert.assertNull(item.getRandomStringMap());
        Assert.assertNull(item.randomStringListWithNoAccessors);
        Assert.assertNull(item.getRandomStringListWithNoSetter());
        Assert.assertNotNull(item.getPostDate());
        Assert.assertEquals(2014, item.getPostDate().getYear());
        Assert.assertNotNull(item.getTeaserImage());
        Assert.assertEquals(1, item.getTeaserImage().size());
        Assert.assertNotNull(item.getCoffeeProcessingTechniques());
        Assert.assertEquals("Coffee processing techniques", item.getCoffeeProcessingTechniques().getTitle());
        Assert.assertNotNull(item.getArabicaBourbonOrigin());
        Assert.assertEquals("Origins of Arabica Bourbon", item.getArabicaBourbonOrigin().getSystem().getName());
        Assert.assertNotNull(item.getArticleItems());
        Assert.assertEquals(2, item.getArticleItems().size());
        Assert.assertNotNull(item.getAllModularContent());
        Assert.assertEquals(2, item.getAllModularContent().size());
        Assert.assertNotNull(item.getAllModularContentMap());
        Assert.assertEquals(2, item.getAllModularContentMap().size());
    }

    @Test
    public void testGetStronglyTypedItemByRegisteringType() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "items/on_roasts"),
                (request, response, context) -> response.setEntity(
                        new InputStreamEntity(
                                this.getClass().getResourceAsStream("SampleContentItem.json")
                        )
                ));
        HttpHost httpHost = this.start();
        DeliveryClient client = new DeliveryClient(projectId);
        client.registerType("article", ArticleItem.class);

        //modify default baseurl to point to test server, this is private so using reflection
        String testServerUri = httpHost.toURI() + "/%s";
        Field deliveryOptionsField = client.getClass().getDeclaredField("deliveryOptions");
        deliveryOptionsField.setAccessible(true);
        ((DeliveryOptions) deliveryOptionsField.get(client)).setProductionEndpoint(testServerUri);

        Object itemObj = client.getItem("on_roasts", Object.class);
        Assert.assertNotNull(itemObj);
        Assert.assertTrue(itemObj instanceof ArticleItem);
    }

    @Test
    public void testGetStronglyTypedItemByRegisteringMapping() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "items/on_roasts"),
                (request, response, context) -> response.setEntity(
                        new InputStreamEntity(
                                this.getClass().getResourceAsStream("SampleContentItem.json")
                        )
                ));
        HttpHost httpHost = this.start();
        DeliveryClient client = new DeliveryClient(projectId);
        client.registerType(ArticleItem.class);

        //modify default baseurl to point to test server, this is private so using reflection
        String testServerUri = httpHost.toURI() + "/%s";
        Field deliveryOptionsField = client.getClass().getDeclaredField("deliveryOptions");
        deliveryOptionsField.setAccessible(true);
        ((DeliveryOptions) deliveryOptionsField.get(client)).setProductionEndpoint(testServerUri);

        Object itemObj = client.getItem("on_roasts", Object.class);
        Assert.assertNotNull(itemObj);
        Assert.assertTrue(itemObj instanceof ArticleItem);
    }

    @Test
    public void testGetStronglyTypedItemByClasspathScan() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "items/on_roasts"),
                (request, response, context) -> response.setEntity(
                        new InputStreamEntity(
                                this.getClass().getResourceAsStream("SampleContentItem.json")
                        )
                ));
        HttpHost httpHost = this.start();
        DeliveryClient client = new DeliveryClient(projectId);
        client.scanClasspathForMappings("com.kenticocloud");

        //modify default baseurl to point to test server, this is private so using reflection
        String testServerUri = httpHost.toURI() + "/%s";
        Field deliveryOptionsField = client.getClass().getDeclaredField("deliveryOptions");
        deliveryOptionsField.setAccessible(true);
        ((DeliveryOptions) deliveryOptionsField.get(client)).setProductionEndpoint(testServerUri);

        Object itemObj = client.getItem("on_roasts", Object.class);
        Assert.assertNotNull(itemObj);
        Assert.assertTrue(itemObj instanceof ArticleItem);
    }

    @Test
    public void testGetStronglyTypedItems() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "items"),
                (request, response, context) -> response.setEntity(
                        new InputStreamEntity(
                                this.getClass().getResourceAsStream("SampleContentItemList.json")
                        )
                ));
        HttpHost httpHost = this.start();
        DeliveryClient client = new DeliveryClient(projectId);

        //modify default baseurl to point to test server, this is private so using reflection
        String testServerUri = httpHost.toURI() + "/%s";
        Field deliveryOptionsField = client.getClass().getDeclaredField("deliveryOptions");
        deliveryOptionsField.setAccessible(true);
        ((DeliveryOptions) deliveryOptionsField.get(client)).setProductionEndpoint(testServerUri);

        List<ArticleItem> items = client.getItems(ArticleItem.class);
        Assert.assertNotNull(items);
    }


    @Test
    public void testGetPreviewItem() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";
        String previewApiKey = "preview_api_key";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "items/on_roasts"),
                (request, response, context) -> {
                    Assert.assertEquals(
                            "Bearer preview_api_key",
                            request.getHeaders("Authorization")[0].getValue());
                    response.setEntity(
                            new InputStreamEntity(
                                    this.getClass().getResourceAsStream("SampleContentItem.json")
                            )
                    );
                });
        HttpHost httpHost = this.start();
        DeliveryClient client = new DeliveryClient(projectId, previewApiKey);

        //modify default baseurl to point to test server, this is private so using reflection
        String testServerUri = httpHost.toURI() + "/%s";
        Field deliveryOptionsField = client.getClass().getDeclaredField("deliveryOptions");
        deliveryOptionsField.setAccessible(true);
        ((DeliveryOptions) deliveryOptionsField.get(client)).setPreviewEndpoint(testServerUri);

        ContentItemResponse item = client.getItem("on_roasts");
        Assert.assertNotNull(item);
    }

    @Test
    public void testWaitForLoadingNewContentHeader() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "items/on_roasts"),
                (request, response, context) -> {
                    Assert.assertEquals(
                            "true",
                            request.getHeaders("X-KC-Wait-For-Loading-New-Content")[0].getValue());
                    response.setEntity(
                            new InputStreamEntity(
                                    this.getClass().getResourceAsStream("SampleContentItem.json")
                            )
                    );
                });
        HttpHost httpHost = this.start();
        String testServerUri = httpHost.toURI() + "/%s";
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setProjectId(projectId);
        deliveryOptions.setProductionEndpoint(testServerUri);
        deliveryOptions.setWaitForLoadingNewContent(true);
        DeliveryClient client = new DeliveryClient(deliveryOptions);

        ContentItemResponse item = client.getItem("on_roasts");
        Assert.assertNotNull(item);
    }

    @Test
    public void testKenticoException() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "items/on_roatst"),
                (request, response, context) -> {
                    response.setStatusCode(404);
                    response.setEntity(
                            new InputStreamEntity(
                                    this.getClass().getResourceAsStream("SampleKenticoError.json")
                            )
                    );
                });
        HttpHost httpHost = this.start();
        DeliveryClient client = new DeliveryClient(projectId);

        //modify default baseurl to point to test server, this is private so using reflection
        String testServerUri = httpHost.toURI() + "/%s";
        Field deliveryOptionsField = client.getClass().getDeclaredField("deliveryOptions");
        deliveryOptionsField.setAccessible(true);
        ((DeliveryOptions) deliveryOptionsField.get(client)).setProductionEndpoint(testServerUri);

        try {
            ContentItemResponse item = client.getItem("on_roatst");
            Assert.fail("Expected KenticoErrorException");
        } catch (KenticoErrorException e) {
            Assert.assertEquals("The requested content item 'on_roatst' was not found.", e.getMessage());
            Assert.assertEquals("The requested content item 'on_roatst' was not found.", e.getKenticoError().getMessage());
        }
    }

    @Test
    public void testKentico500Exception() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "items/on_roatst"),
                (request, response, context) -> response.setStatusCode(500));
        HttpHost httpHost = this.start();
        DeliveryClient client = new DeliveryClient(projectId);

        //modify default baseurl to point to test server, this is private so using reflection
        String testServerUri = httpHost.toURI() + "/%s";
        Field deliveryOptionsField = client.getClass().getDeclaredField("deliveryOptions");
        deliveryOptionsField.setAccessible(true);
        ((DeliveryOptions) deliveryOptionsField.get(client)).setProductionEndpoint(testServerUri);

        try {
            ContentItemResponse item = client.getItem("on_roatst");
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertEquals("Unknown error with Kentico API.  Kentico is likely suffering site issues.", e.getMessage());
        }
    }

    @Test
    public void testGetTypesWithParams() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "types"),
                (request, response, context) -> {
                    String uri = String.format("http://testserver%s", request.getRequestLine().getUri());

                    List<NameValuePair> nameValuePairs =
                            URLEncodedUtils.parse(URI.create(uri), Charset.defaultCharset());
                    Map<String, String> params = convertNameValuePairsToMap(nameValuePairs);
                    Assert.assertEquals(1, params.size());
                    Assert.assertEquals("/path1/path2/test-article",params.get("elements.url_pattern"));
                    response.setEntity(
                            new InputStreamEntity(
                                    this.getClass().getResourceAsStream("SampleContentTypeList.json")
                            )
                    );
                });
        HttpHost httpHost = this.start();
        String testServerUri = httpHost.toURI() + "/%s";
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setProjectId(projectId);
        deliveryOptions.setProductionEndpoint(testServerUri);

        DeliveryClient client = new DeliveryClient(deliveryOptions);
        client.setContentLinkUrlResolver(Link::getUrlSlug);

        List<NameValuePair> urlPattern = DeliveryParameterBuilder.params().filterEquals("elements.url_pattern", "/path1/path2/test-article").build();
        ContentTypesListingResponse types = client.getTypes(urlPattern);

        Assert.assertNotNull(types);
    }

    @Test
    public void testGetTypes() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "types"),
                (request, response, context) -> response.setEntity(
                        new InputStreamEntity(
                                this.getClass().getResourceAsStream("SampleContentTypeList.json")
                        )
                ));
        HttpHost httpHost = this.start();
        DeliveryClient client = new DeliveryClient(projectId);

        //modify default baseurl to point to test server, this is private so using reflection
        String testServerUri = httpHost.toURI() + "/%s";
        Field deliveryOptionsField = client.getClass().getDeclaredField("deliveryOptions");
        deliveryOptionsField.setAccessible(true);
        ((DeliveryOptions) deliveryOptionsField.get(client)).setProductionEndpoint(testServerUri);

        ContentTypesListingResponse types = client.getTypes();
        Assert.assertNotNull(types);
    }

    @Test
    public void testGetTypeWithParams() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "types/coffee"),
                (request, response, context) -> {
                    String uri = String.format("http://testserver%s", request.getRequestLine().getUri());

                    List<NameValuePair> nameValuePairs =
                            URLEncodedUtils.parse(URI.create(uri), Charset.defaultCharset());
                    Map<String, String> params = convertNameValuePairsToMap(nameValuePairs);
                    Assert.assertEquals(1, params.size());
                    Assert.assertEquals("/path1/path2/test-article",params.get("elements.url_pattern"));
                    response.setEntity(
                            new InputStreamEntity(
                                    this.getClass().getResourceAsStream("SampleContentType.json")
                            )
                    );
                });
        HttpHost httpHost = this.start();
        String testServerUri = httpHost.toURI() + "/%s";
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setProjectId(projectId);
        deliveryOptions.setProductionEndpoint(testServerUri);

        DeliveryClient client = new DeliveryClient(deliveryOptions);
        client.setContentLinkUrlResolver(Link::getUrlSlug);

        List<NameValuePair> urlPattern = DeliveryParameterBuilder.params().filterEquals("elements.url_pattern", "/path1/path2/test-article").build();
        ContentType type = client.getType("coffee", urlPattern);

        Assert.assertNotNull(type);
    }

    @Test
    public void testGetType() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "types/coffee"),
                (request, response, context) -> response.setEntity(
                        new InputStreamEntity(
                                this.getClass().getResourceAsStream("SampleContentType.json")
                        )
                ));
        HttpHost httpHost = this.start();
        DeliveryClient client = new DeliveryClient(projectId);

        //modify default baseurl to point to test server, this is private so using reflection
        String testServerUri = httpHost.toURI() + "/%s";
        Field deliveryOptionsField = client.getClass().getDeclaredField("deliveryOptions");
        deliveryOptionsField.setAccessible(true);
        ((DeliveryOptions) deliveryOptionsField.get(client)).setProductionEndpoint(testServerUri);

        ContentType type = client.getType("coffee");
        Assert.assertNotNull(type);
    }

    @Test
    public void testGetTypeElementWithParams() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "types/coffee/elements/processing"),
                (request, response, context) -> {
                    String uri = String.format("http://testserver%s", request.getRequestLine().getUri());

                    List<NameValuePair> nameValuePairs =
                            URLEncodedUtils.parse(URI.create(uri), Charset.defaultCharset());
                    Map<String, String> params = convertNameValuePairsToMap(nameValuePairs);
                    Assert.assertEquals(1, params.size());
                    Assert.assertEquals("/path1/path2/test-article",params.get("elements.url_pattern"));
                    response.setEntity(
                            new InputStreamEntity(
                                    this.getClass().getResourceAsStream("SampleContentTypeElementResponse.json")
                            )
                    );
                });
        HttpHost httpHost = this.start();
        String testServerUri = httpHost.toURI() + "/%s";
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setProjectId(projectId);
        deliveryOptions.setProductionEndpoint(testServerUri);

        DeliveryClient client = new DeliveryClient(deliveryOptions);
        client.setContentLinkUrlResolver(Link::getUrlSlug);

        List<NameValuePair> urlPattern = DeliveryParameterBuilder.params().filterEquals("elements.url_pattern", "/path1/path2/test-article").build();
        Element element = client.getContentTypeElement("coffee", "processing", urlPattern);

        Assert.assertNotNull(element);
        Assert.assertEquals("processing", element.getCodeName());
        Assert.assertTrue(element instanceof MultipleChoiceElement);
    }

    @Test
    public void testGetTypeElement() throws Exception {
        String projectId = "02a70003-e864-464e-b62c-e0ede97deb8c";

        this.serverBootstrap.registerHandler(
                String.format("/%s/%s", projectId, "types/coffee/elements/processing"),
                (request, response, context) -> response.setEntity(
                        new InputStreamEntity(
                                this.getClass().getResourceAsStream("SampleContentTypeElementResponse.json")
                        )
                ));
        HttpHost httpHost = this.start();
        DeliveryClient client = new DeliveryClient(projectId);

        //modify default baseurl to point to test server, this is private so using reflection
        String testServerUri = httpHost.toURI() + "/%s";
        Field deliveryOptionsField = client.getClass().getDeclaredField("deliveryOptions");
        deliveryOptionsField.setAccessible(true);
        ((DeliveryOptions) deliveryOptionsField.get(client)).setProductionEndpoint(testServerUri);

        Element element = client.getContentTypeElement("coffee", "processing");
        Assert.assertNotNull(element);
        Assert.assertEquals("processing", element.getCodeName());
        Assert.assertTrue(element instanceof MultipleChoiceElement);
    }

    @Test
    public void testExceptionWhenProjectIdIsNull() {
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        try {
            DeliveryClient client = new DeliveryClient(deliveryOptions);
            Assert.fail("Expected IllegalArgumentException due to null Project Id");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Kentico Cloud project identifier is not specified.", e.getMessage());
        }
    }

    @Test
    public void testExceptionWhenProjectIdIsEmpty() {
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setProjectId("");
        try {
            DeliveryClient client = new DeliveryClient(deliveryOptions);
            Assert.fail("Expected IllegalArgumentException due to empty Project Id");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Kentico Cloud project identifier is not specified.", e.getMessage());
        }
    }

    @Test
    public void testExceptionWhenDeliveryOptionsIsNull() {
        try {
            DeliveryOptions deliveryOptions = null;
            DeliveryClient client = new DeliveryClient(deliveryOptions);
            Assert.fail("Expected IllegalArgumentException due to null Delivery options");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("The Delivery options object is not specified.", e.getMessage());
        }
    }

    @Test
    public void testExceptionWhenInvalidProjectIdProvided() {
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setProjectId("Invalid GUID");
        try {
            DeliveryClient client = new DeliveryClient(deliveryOptions);
            Assert.fail("Expected IllegalArgumentException due to invalid Project ID");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Provided string is not a valid project identifier (Invalid GUID).  Have you accidentally passed the Preview API key instead of the project identifier?", e.getMessage());
        }
    }

    @Test
    public void testExceptionWhenNullPreviewApiKeyProvided() {
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setProjectId("02a70003-e864-464e-b62c-e0ede97deb8c");
        deliveryOptions.setUsePreviewApi(true);
        try {
            DeliveryClient client = new DeliveryClient(deliveryOptions);
            Assert.fail("Expected IllegalArgumentException due to null Preview API Key");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("The Preview API key is not specified.", e.getMessage());
        }
    }

    @Test
    public void testExceptionWhenEmptyPreviewApiKeyProvided() {
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setProjectId("02a70003-e864-464e-b62c-e0ede97deb8c");
        deliveryOptions.setUsePreviewApi(true);
        deliveryOptions.setPreviewApiKey("");
        try {
            DeliveryClient client = new DeliveryClient(deliveryOptions);
            Assert.fail("Expected IllegalArgumentException due to empty Preview API Key");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("The Preview API key is not specified.", e.getMessage());
        }
    }

    private Map<String, String> convertNameValuePairsToMap(List<NameValuePair> nameValuePairs) {
        HashMap<String, String> map = new HashMap<>();
        for (NameValuePair nameValuePair : nameValuePairs) {
            map.put(nameValuePair.getName(), nameValuePair.getValue());
        }
        return map;
    }

}
