<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Unauthorized Access | Mystic Home Creation</title>
    <link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
    <style>
        @keyframes shake {
            0% { transform: translateX(0); }
            25% { transform: translateX(-5px); }
            50% { transform: translateX(5px); }
            75% { transform: translateX(-5px); }
            100% { transform: translateX(0); }
        }
        .shake-animation {
            animation: shake 0.5s;
        }
        .lock-animation:hover {
            transform: translateY(-3px);
            transition: transform 0.3s ease;
        }
    </style>
</head>
<body class="bg-gray-50 font-sans leading-normal tracking-normal">
    <div class="container mx-auto px-4 h-screen flex items-center justify-center">
        <div class="max-w-md w-full bg-white rounded-xl shadow-lg overflow-hidden">
            <!-- Error Header with Lock Icon -->
            <div class="bg-yellow-50 p-6 text-center border-b border-yellow-100">
                <div class="lock-animation inline-block mb-2">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 text-yellow-500 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                </div>
                <h1 class="text-2xl font-bold text-gray-800">Access Denied</h1>
                <p class="text-gray-600 mt-1">You don't have permission to access this resource.</p>
            </div>

            <div class="p-6 space-y-6">
                <!-- Explanation Section -->
                <div class="bg-gray-50 border-l-4 border-yellow-400 p-4 rounded">
                    <div class="flex">
                        <div class="flex-shrink-0">
                            <svg class="h-5 w-5 text-yellow-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                                <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                            </svg>
                        </div>
                        <div class="ml-3">
                            <h3 class="text-sm font-medium text-yellow-800">Error Code: 401 Unauthorized</h3>
                            <div class="mt-2 text-sm text-yellow-700">
                                <p>This might be because:</p>
                                <ul class="list-disc pl-5 space-y-1 mt-2">
                                    <li>You need to log in first</li>
                                    <li>Your session may have expired</li>
                                    <li>Your account lacks the required permissions</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Action Buttons -->
                <div class="flex flex-col space-y-3">
                    <a href="<%= request.getContextPath() %>/Landing/login" class="w-full inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M3 3a1 1 0 011 1v12a1 1 0 11-2 0V4a1 1 0 011-1zm7.707 3.293a1 1 0 010 1.414L9.414 9H17a1 1 0 110 2H9.414l1.293 1.293a1 1 0 01-1.414 1.414l-3-3a1 1 0 010-1.414l3-3a1 1 0 011.414 0z" clip-rule="evenodd" />
                        </svg>
                        Sign In
                    </a>
                    
                    <a href="javascript:history.back()" class="w-full inline-flex justify-center py-2 px-4 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors">
                        Go Back
                    </a>
                    
                    <a href="<%= request.getContextPath() %>/Landing" class="text-center text-sm text-indigo-600 hover:text-indigo-800 transition-colors">
                        Return to Home
                    </a>
                </div>
            </div>
            
            <!-- Footer Help Section -->
            <div class="px-6 py-4 bg-gray-50 border-t border-gray-200 text-center">
                <p class="text-sm text-gray-600">
                    If you believe you should have access, please
                    <a href="<%= request.getContextPath() %>/web/contact" class="text-indigo-600 hover:underline">contact support</a>.
                </p>
            </div>
        </div>
    </div>
    
    <script>
        // Add shake animation to the lock icon when page loads
        document.addEventListener('DOMContentLoaded', function() {
            setTimeout(function() {
                const lockIcon = document.querySelector('.lock-animation');
                lockIcon.classList.add('shake-animation');
            }, 500);
        });
    </script>
</body>
</html>