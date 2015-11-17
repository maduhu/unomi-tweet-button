<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="resource" type="org.jahia.services.render.Resource"--%>


<c:set var="scriptURL" value='${renderContext.request.secure ? "https://localhost:9443" : "http://localhost:8181"}'/>
<c:set var="pageName" value='${fn:escapeXml(resource.node.displayableName)}'/>


<template:addResources type="inlinejavascript">
    <script type="application/javascript">
        window.digitalData = window.digitalData || {
                    "scope": "${renderContext.site.siteKey}",
                    "site": {
                        "siteInfo": {
                            "siteID": "${resource.node.resolveSite.identifier}"
                        }
                    },
                    "page": {
                        "pageInfo": {
                            "pageID": "${resource.node.identifier}",
                            "pageName": "${pageName}",
                            "pagePath": "${resource.node.path}",
                            "destinationURL": document.location.href,
                            "referringURL": document.referrer,
                            "language": "${resource.locale}"
                        }
                    },
                    "contextServerPublicUrl": "${scriptURL}"
                };
    </script>
</template:addResources>
<template:addResources type="javascript" resources="twitterButton.js"/>

<a href="https://twitter.com/share" class="twitter-share-button" data-via="jahia" data-related="jahia" data-hashtags="jahia">Tweet</a>