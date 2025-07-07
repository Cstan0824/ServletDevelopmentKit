<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ page import="java.util.Date" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 Internal Server Error | Mystichome Creations</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/Content/css/output.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/Content/css/swiper.css"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" />
    <link href="https://unpkg.com/aos@2.3.1/dist/aos.css" rel="stylesheet">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/3.12.2/gsap.min.js"></script>
    <style>
        @keyframes float {
            0% { transform: translateY(0px) rotate(0deg); }
            50% { transform: translateY(-20px) rotate(5deg); }
            100% { transform: translateY(0px) rotate(0deg); }
        }
        .float-animation {
            animation: float 6s ease-in-out infinite;
        }
    </style>
</head>
<body class="bg-grey1 font-poppins leading-normal tracking-normal">
    <div class="container mx-auto px-4 h-screen flex items-center justify-center">
        <div class="max-w-md w-full bg-white rounded-xl shadow-lg overflow-hidden p-8 space-y-8">
            <!-- Error Icon/Image -->
            <div class="text-center">
                <div class="mx-auto w-24 h-24 float-animation">
                    <svg class="w-full h-full text-red3" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
                        <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"></path>
                    </svg>
                </div>
                <h1 class="text-3xl font-bold text-grey5 mt-4">Oops! Something went wrong</h1>
                <p class="text-grey4 text-lg mt-2">Our server encountered an unexpected error</p>
            </div>
            
            <!-- Error Details -->
            <div class="bg-red1 p-4 rounded-lg border border-red2">
                <p class="text-red4 font-medium">Error 500: Internal Server Error</p>
            </div>
            
            <!-- Helpful Message -->
            <div class="space-y-4">
                <p class="text-grey4">Here's what you can try:</p>
                <ul class="list-disc list-inside text-grey4 space-y-2 pl-4">
                    <li>Refresh the page</li>
                    <li>Try again in a few moments</li>
                    <li>Return to homepage and try a different action</li>
                    <li>Contact support if the issue persists</li>
                </ul>
            </div>
            
            <!-- Action Buttons -->
            <div class="flex flex-col sm:flex-row space-y-3 sm:space-y-0 sm:space-x-3">
                <a href="<%= request.getContextPath() %>/Landing" 
                   class="flex-1 px-4 py-2 bg-darkYellow text-white text-center font-medium rounded-md hover:bg-brown3 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-brown3 transition-colors">
                    Return Home
                </a>
                <a href="<%= request.getContextPath() %>/Landing" 
                   class="flex-1 px-4 py-2 bg-white text-darkYellow text-center font-medium rounded-md border border-darkYellow hover:bg-lightYellow focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-darkYellow transition-colors">
                    Contact Us
                </a>
            </div>
        </div>
    </div>
    
    <script>
        // Optional: Log the error to your analytics or monitoring system
        console.log("500 error page viewed at " + new Date().toString());
        
        // Optional: Auto-refresh after a certain timeout (uncomment if desired)
        /*
        setTimeout(function() {
            window.location.href = "<%= request.getContextPath() %>";
        }, 30000); // Redirect after 30 seconds
        */
    </script>
</body>
</html>