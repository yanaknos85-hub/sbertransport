import { useState, useEffect } from 'react';

const useCount = (initialValue = 0): {
  count: number;
  setCount: (count: number) => void;
} => {
  const [count, setCount] = useState(initialValue);

  useEffect(() => {
    if (count === 0) return;

    const countDown = () => setCount(prev => prev - 1);
    const timer = setTimeout(countDown, 1000);

    return () => {
      clearTimeout(timer);
    };
  }, [count]);

  return { count, setCount };
};

export default useCount;
