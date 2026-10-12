import { render, screen } from '@testing-library/react';
import App from './App';

test('renders app title', () => {
  render(<App />);
  const titleElement = screen.getByText(/Raahi/i);
  expect(titleElement).toBeInTheDocument();
});

test('renders tagline', () => {
  render(<App />);
  const taglineElement = screen.getByText(/Plan.*Travel.*Explore/i);
  expect(taglineElement).toBeInTheDocument();
});