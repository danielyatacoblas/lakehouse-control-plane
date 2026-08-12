import { render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import App from './App';
import { project } from './project';

describe('operations console', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('renders the project content and exposes the demo fallback', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('backend offline')));

    render(<App />);

    expect(screen.getByText(project.name)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: project.actionLabel })).toBeEnabled();
    expect(screen.getByText(project.metrics[0].value)).toBeInTheDocument();
    await waitFor(() => expect(screen.getByText('Modo demostración')).toBeInTheDocument());
  });
});
