import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import PdfService from '../services/pdfService';
// import './PdfDetailPage.css'; // We can create and import a CSS file later for styling

const PdfDetailPage = () => {
  const { id } = useParams(); // Get PDF ID from URL
  const [pdfDetails, setPdfDetails] = useState(null);
  const [comments, setComments] = useState([]);
  const [isLoading, setIsLoading] = useState(true); // For overall page details
  const [error, setError] = useState(null); // For overall page error

  // State for PDF blob loading into iframe
  const [iframeSrc, setIframeSrc] = useState('');
  const [isPdfLoadingForIframe, setIsPdfLoadingForIframe] = useState(false);
  const [pdfIframeError, setPdfIframeError] = useState('');

  const [newComment, setNewComment] = useState('');
  const [isSubmittingComment, setIsSubmittingComment] = useState(false);
  const [commentError, setCommentError] = useState('');
  const [shareableLink, setShareableLink] = useState('');
  const [isSharing, setIsSharing] = useState(false);
  const [shareError, setShareError] = useState('');

  // Effect for fetching PDF metadata and comments
  useEffect(() => {
    const fetchDetails = async () => {
      setIsLoading(true);
      setPdfDetails(null); // Reset details on ID change
      setComments([]);
      setError(null);
      setPdfIframeError('');

      // Revoke previous object URL if any, before fetching new details
      if (iframeSrc && iframeSrc.startsWith('blob:')) {
        URL.revokeObjectURL(iframeSrc);
        setIframeSrc('');
      }

      try {
        const response = await PdfService.getPdfDetails(id);
        setPdfDetails(response.data.pdfFile);
        setComments(response.data.comments || []);
      } catch (err) {
        console.error("Failed to fetch PDF details:", err);
        setError(err.response?.data?.message || err.message || 'Failed to load PDF details.');
      } finally {
        setIsLoading(false);
      }
    };

    if (id) {
      fetchDetails();
    }
    // Cleanup on unmount or if ID changes causing re-fetch
    return () => {
      if (iframeSrc && iframeSrc.startsWith('blob:')) {
        URL.revokeObjectURL(iframeSrc);
      }
    };
  }, [id]); // Removed iframeSrc from here, direct cleanup is better

  // Effect for loading PDF blob into iframe once pdfDetails are available
  useEffect(() => {
    const loadPdfBlob = async () => {
      if (pdfDetails && pdfDetails.id) {
        setIsPdfLoadingForIframe(true);
        setPdfIframeError('');

        // Revoke if there's an existing iframeSrc from a previous load (e.g. error then retry)
        if (iframeSrc && iframeSrc.startsWith('blob:')) {
           URL.revokeObjectURL(iframeSrc);
        }

        try {
          // IMPORTANT: You'll need to implement `downloadPdfFile` in `PdfService.js`
          // It should make a GET request to `/api/pdf/download/${pdfDetails.id}`
          // with `responseType: 'blob'` in its axios config.
          const response = await PdfService.downloadPdf(pdfDetails.id);
          const blob = new Blob([response.data], { type: 'application/pdf' });
          const objectUrl = URL.createObjectURL(blob);
          setIframeSrc(objectUrl);
        } catch (err) {
          console.error("Failed to load PDF into iframe:", err);
          setPdfIframeError(err.response?.data?.message || err.message || 'Failed to load PDF preview.');
          setIframeSrc(''); // Clear src on error
        } finally {
          setIsPdfLoadingForIframe(false);
        }
      } else if (!isLoading && id) { // If done loading main details, id exists, but no pdfDetails
        setIsPdfLoadingForIframe(false); // Stop loading iframe if no details
      }
    };

    loadPdfBlob();

    // This effect's cleanup is primarily for when pdfDetails changes, leading to a new blob.
    // The main unmount cleanup is handled by the first useEffect.
    // However, adding it here too ensures cleanup if this effect re-runs before unmount.
    return () => {
      if (iframeSrc && iframeSrc.startsWith('blob:')) {
        URL.revokeObjectURL(iframeSrc);
        // setIframeSrc(''); // Don't clear here, might be needed if component doesn't unmount but pdfDetails change
      }
    };
  }, [pdfDetails]); // Depend on pdfDetails


  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) {
      setCommentError('Comment cannot be empty.');
      return;
    }
    setIsSubmittingComment(true);
    setCommentError('');
    try {
      const response = await PdfService.addComment(id, { text: newComment });
      setComments(prevComments => [...prevComments, response.data]);
      setNewComment('');
    } catch (err) {
      console.error("Failed to add comment:", err);
      setCommentError(err.response?.data?.message || err.message || 'Failed to submit comment.');
    } finally {
      setIsSubmittingComment(false);
    }
  };

  const handleSharePdf = async () => {
    setIsSharing(true);
    setShareError('');
    setShareableLink('');
    try {
      const response = await PdfService.shareFile(id);
      // Assuming response.data.shareableLink is "/api/shared/access/TOKEN"
      // We need to extract the TOKEN and build the correct frontend URL
      const backendShareUrl = response.data.shareUrl; // Changed from shareableLink to shareUrl based on backend
      if (backendShareUrl) {
        const token = backendShareUrl.substring(backendShareUrl.lastIndexOf('/') + 1);
        const fullShareLink = `${window.location.origin}/share/${token}`;
        setShareableLink(fullShareLink);
      } else {
        throw new Error('Share URL not found in response');
      }
    } catch (err) {
      console.error("Failed to share PDF:", err);
      setShareError(err.response?.data?.message || err.message || 'Failed to generate shareable link.');
    } finally {
      setIsSharing(false);
    }
  };

  if (isLoading) {
    return <p className="container">Loading PDF details...</p>;
  }

  if (error) {
    return <p className="container error-message">Error: {error}</p>;
  }

  if (!pdfDetails) {
    return <p className="container">PDF not found.</p>; // Or after loading, if error is not set but no details
  }

  return (
    <div className="container pdf-detail-page">
      <Link to="/dashboard" className="back-to-dashboard-link">&larr; Back to Dashboard</Link>
      
      <div className="pdf-metadata">
        <h2>{pdfDetails.filename}</h2>
        <p><strong>Uploaded by:</strong> {pdfDetails.uploadedBy}</p>
        <p><strong>Uploaded on:</strong> {new Date(pdfDetails.uploadTime).toLocaleString()}</p>
        
        <div className="pdf-viewer-container">
          <h4>PDF Preview</h4>
          {isPdfLoadingForIframe && <p>Loading PDF preview...</p>}
          {pdfIframeError && !isPdfLoadingForIframe && 
            <p className="error-message">Error loading PDF preview: {pdfIframeError}. You can try <a href={`/api/pdf/download/${pdfDetails.id}`} download={pdfDetails.filename}>downloading the PDF</a>.</p>}
          
          {iframeSrc && !isPdfLoadingForIframe && !pdfIframeError && (
            <iframe 
              src={iframeSrc}
              title={pdfDetails.filename}
              width="100%"
              height="700px"
              style={{ border: '1px solid #ccc' }}
            >
              Your browser does not support PDFs. Please download the PDF to view it: <a href={`/api/pdf/download/${pdfDetails.id}`} download={pdfDetails.filename}>Download PDF</a>
            </iframe>
          )}
          {!isPdfLoadingForIframe && !iframeSrc && !pdfIframeError && (
             <p>Preview not available. Try <a href={`/api/pdf/download/${pdfDetails.id}`} download={pdfDetails.filename}>downloading the PDF</a>.</p>
          )}
        </div>

        <div className="share-pdf-section" style={{ marginTop: '20px' }}>
          <h4>Share this PDF</h4>
          <button onClick={handleSharePdf} disabled={isSharing}>
            {isSharing ? 'Generating Link...' : 'Generate Shareable Link'}
          </button>
          {shareError && <p className="error-message share-error">{shareError}</p>}
          {shareableLink && (
            <div className="shareable-link-container" style={{ marginTop: '10px' }}>
              <p>Share this link:</p>
              <input type="text" value={shareableLink} readOnly style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }} />
            </div>
          )}
        </div>

      </div>

      <div className="comments-section">
        <h3>Comments</h3>
        {comments.length === 0 ? (
          <p>No comments yet.</p>
        ) : (
          <ul className="comments-list">
            {comments.map(comment => (
              <li key={comment.id} className="comment-item">
                <p className="comment-text">{comment.text}</p>
                <p className="comment-meta">
                  By: <strong>{comment.username}</strong> on {new Date(comment.commentTime).toLocaleString()}
                </p>
              </li>
            ))}
          </ul>
        )}
        <div className="add-comment-form">
          <h4>Add a Comment</h4>
          <form onSubmit={handleCommentSubmit}>
            <textarea 
              value={newComment} 
              onChange={(e) => setNewComment(e.target.value)} 
              placeholder="Write your comment here..." 
              rows="4"
              disabled={isSubmittingComment}
            />
            {commentError && <p className="error-message comment-error">{commentError}</p>}
            <button type="submit" disabled={isSubmittingComment}>
              {isSubmittingComment ? 'Submitting...' : 'Submit Comment'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default PdfDetailPage;
