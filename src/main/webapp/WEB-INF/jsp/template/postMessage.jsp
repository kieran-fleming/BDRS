<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
    <body onload='callback()'>
        <script type='text/javascript'>
            function callback() {
                parent.postMessage('${message}','*');
            };
        </script>
    </body>
</html>