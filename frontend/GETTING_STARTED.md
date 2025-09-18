# 🚀 Getting Started with Clothing Store Frontend

## 📋 Prerequisites

Before you begin, ensure you have the following installed:
- **Node.js** (version 18 or higher)
- **npm** (version 8 or higher)
- **Git**

## 🛠️ Installation Steps

### 1. Navigate to Frontend Directory
```bash
cd frontend
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Start Development Server
```bash
npm run dev
```

The application will be available at `http://localhost:3000`

## 🔧 Available Scripts

| Script | Description |
|--------|-------------|
| `npm run dev` | Start development server |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build |
| `npm run lint` | Run ESLint |
| `npm run lint:fix` | Fix ESLint issues |

## 🌐 Backend Connection

The frontend is configured to connect to the Spring Boot backend running on `http://localhost:8080`. Make sure your backend is running before starting the frontend.

### API Endpoints Used:
- `GET /api/users` - User management
- `GET /api/sizes` - Size management  
- `GET /api/colors` - Color management
- `GET /api/test/connection` - Test connection

## 📱 Features Overview

### 🏠 Dashboard
- Overview statistics
- Recent activities
- Quick action buttons
- Real-time metrics

### 👥 User Management
- View all users with pagination
- Search and filter users
- Add new users
- Edit user information
- Delete users
- Role-based filtering (Admin/Customer)

### 🏷️ Size Management
- View all sizes
- Add new sizes (S, M, L, XL, etc.)
- Edit size names
- Delete sizes
- Search functionality

### 🎨 Color Management
- View all colors with preview
- Add new colors
- Color picker integration
- Edit color names
- Delete colors
- Visual color previews

## 🎨 UI/UX Features

- **Modern Design**: Clean, professional interface
- **Responsive**: Works on desktop, tablet, and mobile
- **Dark/Light Theme**: Configurable theme system
- **Animations**: Smooth transitions and hover effects
- **Accessibility**: WCAG compliant components
- **Loading States**: Proper loading indicators
- **Error Handling**: User-friendly error messages
- **Notifications**: Toast messages for actions

## 🔧 Customization

### Theme Configuration
Edit `src/App.tsx` to customize the theme:

```typescript
const antdTheme = {
  token: {
    colorPrimary: '#1890ff', // Change primary color
    borderRadius: 6,         // Change border radius
  },
};
```

### Adding New Pages
1. Create component in `src/pages/`
2. Add route in `src/App.tsx`
3. Add menu item in `src/components/Layout/AppLayout.tsx`

### API Integration
- Add new API functions in `src/services/api.ts`
- Use custom hooks in `src/hooks/useApi.ts`
- Define types in `src/types/index.ts`

## 🐛 Troubleshooting

### Common Issues

**Port 3000 already in use:**
```bash
# Kill process on port 3000
npx kill-port 3000
# Or use different port
npm run dev -- --port 3001
```

**API connection issues:**
- Ensure backend is running on `http://localhost:8080`
- Check CORS configuration in backend
- Verify API endpoints are accessible

**Build errors:**
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
```

**TypeScript errors:**
```bash
# Check TypeScript configuration
npx tsc --noEmit
```

## 📊 Performance Tips

- Use React.memo for expensive components
- Implement lazy loading for routes
- Optimize images and assets
- Use proper key props in lists
- Avoid unnecessary re-renders

## 🔒 Security Considerations

- Input validation on all forms
- XSS protection
- CSRF token handling
- Secure API communication
- Role-based access control

## 📱 Mobile Optimization

The app is fully responsive with:
- Touch-friendly buttons
- Swipe gestures
- Mobile-optimized tables
- Responsive navigation
- Adaptive layouts

## 🚀 Deployment

### Build for Production
```bash
npm run build
```

### Deploy to Vercel
```bash
npx vercel --prod
```

### Deploy to Netlify
```bash
npm run build
# Upload dist folder to Netlify
```

## 📞 Support

If you encounter any issues:
1. Check the console for errors
2. Verify backend connection
3. Review the documentation
4. Create an issue in the repository

## 🎯 Next Steps

After getting the frontend running:
1. Explore the different management pages
2. Test the CRUD operations
3. Customize the theme and styling
4. Add new features as needed
5. Deploy to production

Happy coding! 🎉
