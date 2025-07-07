<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>404 Page Not Found | Mystichome Creations</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/Content/css/output.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/Content/css/swiper.css"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" />
    <link href="https://unpkg.com/aos@2.3.1/dist/aos.css" rel="stylesheet">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/3.12.2/gsap.min.js"></script>
    <style>
        .search-animation {
            transition: all 0.3s ease;
        }
        .search-animation:hover {
            transform: translateY(-2px);
        }
        @keyframes float {
            0% { transform: translateY(0px) rotate(0deg); }
            50% { transform: translateY(-15px) rotate(5deg); }
            100% { transform: translateY(0px) rotate(0deg); }
        }
        .float-animation {
            animation: float 6s ease-in-out infinite;
        }
    </style>
</head>
<body class="bg-grey1 font-poppins leading-normal tracking-normal min-h-screen">
    <div class="container mx-auto max-w-4xl px-4 py-8 flex items-center justify-center min-h-screen">
        <div class="max-w-lg w-full bg-white rounded-xl shadow-lg overflow-hidden">
            <div class="p-8 space-y-8">
                <!-- Error Icon/Image -->
                <div class="text-center">
                    <div class="float-animation mx-auto w-40 h-40 relative">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round" class="w-full h-full text-darkYellow">
                            <path d="M12 8l6-6H6l6 6z"></path>
                            <path d="M15 21H9a1 1 0 01-1-1v-9l4-5 4 5v9a1 1 0 01-1 1z"></path>
                            <path d="M12 7v5" class="text-darkYellow" stroke-width="2"></path>
                        </svg>
                        <div class="absolute bottom-5 right-5 bg-red2 text-white rounded-full w-10 h-10 flex items-center justify-center font-bold text-xs">404</div>
                    </div>
                    
                    <h1 class="text-3xl font-bold text-gray-900 mt-4">Page Not Found</h1>
                    <p class="text-grey4 mt-2">The page you're looking for doesn't exist or has been moved</p>
                </div>
                
                <!-- Possible Reasons -->
                <div class="bg-yellow-50 p-4 rounded-lg">
                    <p class="text-darkYellow font-medium">Possible reasons:</p>
                    <ul class="text-grey5 text-sm mt-2 pl-5 list-disc">
                        <li>The URL might have been mistyped</li>
                        <li>The page might have been moved or deleted</li>
                    </ul>
                </div>
                
                <!-- Popular Links -->
                <div class="space-y-3">
                    <p class="text-grey5 font-medium">Popular destinations:</p>
                    <div class="grid grid-cols-2 gap-3">
                        <a href="<%= request.getContextPath() %>/Landing" class="text-center bg-grey1 hover:bg-grey2 text-gray-700 py-3 px-4 rounded border border-grey3 transition duration-200">
                            Home
                        </a>
                        <a href="<%= request.getContextPath() %>/product/productPage" class="text-center bg-grey1 hover:bg-grey2 text-gray-700 py-3 px-4 rounded border border-grey3 transition duration-200">
                            Products
                        </a>
                    </div>
                </div>
                
                <!-- Return Button -->
                <div class="mt-6 text-center">
                    <a href="javascript:history.back()" class="inline-flex items-center justify-center px-6 py-3 border border-transparent rounded-md shadow-sm text-base font-medium text-white bg-darkYellow hover:bg-yellow-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500 transition-colors">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clip-rule="evenodd" />
                        </svg>
                        Go Back
                    </a>
                </div>
            </div>
            
            <div class="bg-grey1 px-8 py-4 border-t border-grey3">
                <p class="text-sm text-grey4 text-center">
                    If you believe this is an error, please <a href="<%= request.getContextPath() %>/Landing" class="text-darkYellow hover:text-yellow-600">contact our support team</a>.
                </p>
            </div>
        </div>
    </div>
    
    <script>
        // Track 404 errors for analytics
        if (typeof gtag !== 'undefined') {
            gtag('event', '404_error', {
                'event_category': 'error',
                'event_label': document.location.pathname + document.location.search
            });
        }
    </script>
</body>
</html>