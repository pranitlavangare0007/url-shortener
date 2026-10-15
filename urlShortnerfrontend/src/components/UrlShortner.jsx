
import { useState } from "react";
import api from "../api/api";
import "./UrlShortner.css";

export default function UrlShortner() {
  const [url, setUrl] = useState("");
  const [shortUrl, setShortUrl] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);

  const handleShortUrl = async (e) => {
    e.preventDefault();

    const trimmedUrl = url.trim();

    if (!trimmedUrl) {
      setError("Please enter a URL.");
      return;
    }

    try {
      const parsedUrl = new URL(trimmedUrl);

      if (!["http:", "https:"].includes(parsedUrl.protocol)) {
        throw new Error("Invalid protocol");
      }
    } catch {
      setError("Enter a valid URL starting with http:// or https://.");
      return;
    }

    setLoading(true);
    setError("");
    setShortUrl("");
    setCopied(false);

    try {
      const res = await api.post("/url", { url: trimmedUrl });

      // Supports either a plain string or an object response.
      const result =
        typeof res.data === "string"
          ? res.data
          : res.data.shortUrl ?? res.data.shortUrlString ?? "";

      if (!result) {
        throw new Error("Invalid response");
      }

      setShortUrl(result);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to shorten this URL. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(shortUrl);
      setCopied(true);
    } catch {
      setError("Unable to copy. Please copy the URL manually.");
    }
  };

  return (
    <main className="shortener-page">
      <section className="shortener-card">
        <p className="shortener-eyebrow">URL SHORTENER</p>

        <h1>Make your links shorter.</h1>

        <p className="shortener-description">
          Create a short link that's easier to share and manage.
        </p>

        <form onSubmit={handleShortUrl} className="shortener-form">
          <label htmlFor="original-url">Destination URL</label>

          <input
            id="original-url"
            type="url"
            placeholder="https://example.com/your-long-link"
            value={url}
            onChange={(e) => {
              setUrl(e.target.value);
              setError("");
            }}
            autoComplete="url"
            required
          />

          <button type="submit" disabled={loading}>
            {loading ? "Creating link..." : "Shorten URL"}
          </button>
        </form>

        {error && (
          <p className="shortener-error" role="alert">
            {error}
          </p>
        )}

        {shortUrl && (
          <div className="shortener-result" aria-live="polite">
            <label htmlFor="short-url">Your shortened URL</label>

            <div className="shortener-result-row">
              <input
                id="short-url"
                type="text"
                value={shortUrl}
                readOnly
                onFocus={(e) => e.target.select()}
              />

              <button
                type="button"
                className="copy-button"
                onClick={handleCopy}
              >
                {copied ? "Copied" : "Copy"}
              </button>
            </div>
          </div>
        )}

        
      </section>
    </main>
  );
}
