import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App.jsx';
import { BrowserRouter } from 'react-router-dom';
import AuthenticationContextProvider from './hooks/authentication-hooks.jsx';
// import reportWebVitals from './reportWebVitals';

ReactDOM.render(
  <React.StrictMode>
      <AuthenticationContextProvider>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </AuthenticationContextProvider>
  </React.StrictMode>,
  document.getElementById('root')
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
// reportWebVitals(console.log);
