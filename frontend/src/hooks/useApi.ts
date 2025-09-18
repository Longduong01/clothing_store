import { useState, useEffect } from 'react';
import { message } from 'antd';
import { LoadingState } from '../types';

export function useApi<T>(
  apiCall: () => Promise<T>,
  dependencies: any[] = []
): LoadingState & { data: T | null; refetch: () => void } {
  const [data, setData] = useState<T | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | undefined>();

  const fetchData = async () => {
    try {
      setIsLoading(true);
      setError(undefined);
      const result = await apiCall();
      setData(result);
    } catch (err: any) {
      setError(err.message || 'An error occurred');
      message.error(err.message || 'Failed to fetch data');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, dependencies);

  return {
    data,
    isLoading,
    error,
    refetch: fetchData,
  };
}

export function useMutation<T, P extends any[]>(
  mutationFn: (...params: P) => Promise<T>
): {
  mutate: (...params: P) => Promise<T | undefined>;
  isLoading: boolean;
  error: string | undefined;
} {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | undefined>();

  const mutate = async (...params: P): Promise<T | undefined> => {
    try {
      setIsLoading(true);
      setError(undefined);
      const result = await mutationFn(...params);
      return result;
    } catch (err: any) {
      setError(err.message || 'An error occurred');
      message.error(err.message || 'Operation failed');
      return undefined;
    } finally {
      setIsLoading(false);
    }
  };

  return {
    mutate,
    isLoading,
    error,
  };
}
