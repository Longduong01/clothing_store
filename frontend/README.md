# Clothing Store Frontend

A modern, responsive React frontend for the Clothing Store Management System built with TypeScript, Ant Design, and Vite.

## 🚀 Features

- **Modern UI/UX**: Clean, professional interface with Ant Design components
- **Responsive Design**: Works seamlessly on desktop, tablet, and mobile devices
- **Type Safety**: Full TypeScript support for better development experience
- **State Management**: Efficient state management with custom hooks
- **API Integration**: Seamless integration with Spring Boot backend
- **Real-time Updates**: Live data updates and notifications
- **Accessibility**: WCAG compliant components and interactions

## 📋 Pages & Components

### 🏠 Dashboard
- Overview statistics and metrics
- Recent activities feed
- Quick action buttons
- Real-time data visualization

### 👥 User Management
- Complete CRUD operations for users
- Role-based filtering (Admin/Customer)
- Advanced search functionality
- Bulk operations support
- User statistics and analytics

### 🏷️ Size Management
- Manage product sizes (S, M, L, XL, etc.)
- Visual size tags and previews
- Bulk size operations
- Size usage analytics

### 🎨 Color Management
- Color palette management
- Visual color previews
- Color picker integration
- Hex/RGB color support
- Color usage tracking

## 🛠️ Tech Stack

- **React 18** - Modern React with hooks and concurrent features
- **TypeScript** - Type-safe JavaScript development
- **Ant Design 5** - Enterprise-class UI design language
- **Vite** - Fast build tool and development server
- **React Router 6** - Declarative routing for React
- **Axios** - HTTP client for API requests
- **Day.js** - Lightweight date manipulation library
- **React Color** - Color picker components

## 📦 Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start development server**
   ```bash
   npm run dev
   ```

4. **Build for production**
   ```bash
   npm run build
   ```

## 🔧 Configuration

### Environment Variables
Create a `.env` file in the root directory:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_TITLE=Clothing Store Management
VITE_APP_VERSION=1.0.0
```

### API Configuration
The frontend is configured to proxy API requests to the Spring Boot backend running on `http://localhost:8080`. Update the proxy configuration in `vite.config.ts` if needed.

## 🎨 Customization

### Theme Configuration
Modify the Ant Design theme in `src/App.tsx`:

```typescript
const antdTheme = {
  token: {
    colorPrimary: '#1890ff', // Primary color
    borderRadius: 6,          // Border radius
    // ... other theme tokens
  },
};
```

### Styling
- Global styles: `src/index.css`
- Component-specific styles: Use CSS modules or styled-components
- Ant Design overrides: Modify the theme configuration

## 📱 Responsive Design

The application is fully responsive with breakpoints:
- **Mobile**: < 576px
- **Tablet**: 576px - 768px
- **Desktop**: > 768px

## 🔐 Security Features

- Input validation and sanitization
- XSS protection
- CSRF token handling
- Role-based access control
- Secure API communication

## 🧪 Testing

```bash
# Run tests
npm test

# Run tests with coverage
npm run test:coverage

# Run linting
npm run lint

# Fix linting issues
npm run lint:fix
```

## 📊 Performance

- **Code Splitting**: Automatic route-based code splitting
- **Lazy Loading**: Components loaded on demand
- **Bundle Optimization**: Optimized production builds
- **Caching**: Efficient API response caching
- **Image Optimization**: Optimized image loading

## 🚀 Deployment

### Build for Production
```bash
npm run build
```

### Deploy to Static Hosting
The built files in the `dist` directory can be deployed to any static hosting service:
- Vercel
- Netlify
- AWS S3
- GitHub Pages

### Docker Deployment
```dockerfile
FROM node:18-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation

## 🔄 Updates

### Version 1.0.0
- Initial release
- User, Size, and Color management
- Dashboard with statistics
- Responsive design
- TypeScript support

### Planned Features
- Product management
- Order management
- Inventory tracking
- Advanced analytics
- Multi-language support
- Dark mode theme
