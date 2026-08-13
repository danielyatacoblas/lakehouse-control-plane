import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import App from './App';
import { project } from './project';

describe('operations console', () => {
  afterEach(() => {
    cleanup();
    vi.unstubAllGlobals();
    window.history.replaceState({}, '', '/');
  });

  it('renders the project content and exposes the demo fallback', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('backend offline')));

    render(<App />);

    expect(screen.getByText(project.name)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: project.actionLabel })).toBeEnabled();
    expect(screen.getByText(project.metrics[0].value)).toBeInTheDocument();
    await waitFor(() => expect(screen.getByText('Modo demostración')).toBeInTheDocument());
  });

  it('navigates through every operational view and switches theme', () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('backend offline')));

    render(<App />);

    project.nav.slice(1).forEach((label) => {
      fireEvent.click(screen.getByRole('button', { name: label }));
      expect(screen.getByRole('heading', { name: label, level: 1 })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: label })).toHaveAttribute('aria-current', 'page');
    });

    fireEvent.click(screen.getByRole('button', { name: 'Activar tema claro' }));
    expect(document.querySelector('.app')).toHaveClass('light');
    expect(window.location.search).toContain('theme=light');
  });
});
