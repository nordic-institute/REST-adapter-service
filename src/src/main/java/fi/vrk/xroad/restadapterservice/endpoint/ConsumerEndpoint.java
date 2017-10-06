/**
 * The MIT License
 * Copyright © 2017 Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fi.vrk.xroad.restadapterservice.endpoint;

import fi.vrk.xrd4j.common.member.ConsumerMember;
import fi.vrk.xrd4j.common.member.ProducerMember;
import lombok.extern.slf4j.Slf4j;

/**
 * This class represents consumer endpoint and holds all the information that
 * is needed for publishing it. Consumer endpoint is identified by the
 * combination of HTTP verb and resource path, which is the part after
 * the base URL. E.g. http://base.com/resource-path. Resource path may contain
 * one or more resource id's that are marked as {resourceId}. E.g.
 * http://base.com/resource-path/{resourceId}/str/{resourceId}. The value
 * of the resource id variable is the rest of the URL starting from the first
 * curly bracket "{". E.g. "{resourceId}/str/{resourceId}".
 *
 * Client id is X-Road client id that must be defined on subsystem level:
 * instance.memberClass.memberCode.subsystem. Service id is X-Road service id
 * that must defined on service level:
 * instance.memberClass.memberCode.subsystem.service.version
 *
 * @author Petteri Kivimäki
 */
@Slf4j
public class ConsumerEndpoint extends AbstractEndpoint {

    /**
     * X-Road service id that is called.
     */
    private String clientId;
    /**
     * Resource path is the part after the base URL that identiefies this
     * ConsumerEndpoint.
     */
    private String resourcePath;
    /**
     * Resource id is part of resource path and it identifies a single resource,
     * and therefore it's not fixed. The value of the resource id may vary
     * between requests even if the requests are targeted to the same endpoint
     * and resource path.
     */
    private String resourceId;
    /**
     * X-Road consumer member, the client.
     */
    private ConsumerMember consumer;
    /**
     * X-Road producer member, the service.
     */
    private ProducerMember producer;
    /**
     * Boolean value that indicates if URLs in the response body should
     * be modified to point the Consumer Endpoint instance.
     */
    private boolean modifyUrl;

    /**
     * Boolean value that indicates if request body (if any) should
     * be converted from JSON to XML.
     */
    private boolean convertPost;

    /**
     * Constructs and initializes a new ProviderEndpoint object.
     * @param serviceId unique ID of the service to be called
     * @param clientId client id of this ConsumerEndpoint
     * @param resourcePath resource path of the Adapter Service
     */
    public ConsumerEndpoint(String serviceId, String clientId, String resourcePath) {
        super(serviceId);
        this.clientId = clientId;
        this.resourcePath = resourcePath;
        this.modifyUrl = false;
    }

    /**
     * Returns the resource path of this ConsumerEndpoint.
     * @return resource path of this ConsumerEndpoint
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Sets the resource path of this ConsumerEndpoint.
     * @param resourcePath new value
     */
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * Returns the client id of this ConsumerEndpoint.
     * @return client id of this ConsumerEndpoint
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the client id of this ConsumerEndpoint.
     * @param clientId new value
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Returns the resource id value.
     * @return resource id value
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Sets the resource id value.
     * @param resourceId new value
     */
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * Returns the X-Road consumer member.
     * @return X-Road consumer member
     */
    public ConsumerMember getConsumer() {
        return consumer;
    }

    /**
     * Sets the X-Road consumer member.
     * @param consumer X-Road consumer member
     */
    public void setConsumer(ConsumerMember consumer) {
        this.consumer = consumer;
    }

    /**
     * Returns the X-Road producer member.
     * @return X-Road producer member
     */
    public ProducerMember getProducer() {
        return producer;
    }

    /**
     * Sets the X-Road producer member.
     * @param producer X-Road producer member
     */
    public void setProducer(ProducerMember producer) {
        this.producer = producer;
    }

    /**
     * Returns a boolean value that indicates if URLs in the response body
     * should be modified to point the Consumer Endpoint instance.
     * @return true if and only if the URLs should be modified; otherwise false
     */
    public boolean isModifyUrl() {
        return modifyUrl;
    }

    /**
     * Sets a boolean value that indicates if URLs in the response body
     * should be modified to point the Consumer Endpoint instance.
     * @param modifyUrl
     */
    public void setModifyUrl(boolean modifyUrl) {
        this.modifyUrl = modifyUrl;
    }

    public boolean isConvertPost() {
        return convertPost;
    }

    public void setConvertPost(boolean convertPost) {
        this.convertPost = convertPost;
    }
}
