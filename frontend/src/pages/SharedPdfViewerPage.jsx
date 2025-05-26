import React, { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import PdfService from '../services/pdfService';

const SharedPdfViewerPage = () => {
  const { shareToken } = useParams();
  
  // PDF loading states
  const [isLoadingPdf, setIsLoadingPdf] = useState(true);
  const [pdfUrl, setPdfUrl] = useState('');
  const [pdfDetailsError, setPdfDetailsError] = useState(null); // For errors fetching PDF details or blob

  // PDF Metadata and Comments states
  const [pdfFile, setPdfFile] = useState(null);
  const [comments, setComments] = useState([]);
  const [isLoadingMetadata, setIsLoadingMetadata] = useState(true);

  // New Comment states
  const [guestName, setGuestName] = useState('');
  const [newCommentText, setNewCommentText] = useState('');
  const [isSubmittingComment, setIsSubmittingComment] = useState(false);
  const [commentSubmitError, setCommentSubmitError] = useState('');
  const [commentSubmitSuccess, setCommentSubmitSuccess] = useState('');

  const fetchPdfMetadataAndComments = useCallback(async () => {
    if (!shareToken) return;
    setIsLoadingMetadata(true);
    setPdfDetailsError(null);
    try {
      const response = await PdfService.accessSharedFile(shareToken);
      setPdfFile(response.data.pdfFile);
      setComments(response.data.comments || []);
    } catch (err) {
      console.error("Failed to fetch shared PDF metadata:", err);
      setPdfDetailsError(err.response?.data?.message || err.message || 'Failed to load PDF details. The link may be invalid or expired.');
      setPdfFile(null);
      setComments([]);
    } finally {
      setIsLoadingMetadata(false);
    }
  }, [shareToken]);

  const loadPdfBlob = useCallback(async () => {
    if (!shareToken) return;
    setIsLoadingPdf(true);
    // pdfDetailsError is handled by its own flow and fetchPdfMetadataAndComments.
    // The main useEffect's cleanup function will handle revoking the *previous* pdfUrl.

    try {
      const response = await PdfService.downloadSharedFile(shareToken);
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const objectUrl = URL.createObjectURL(blob);
      setPdfUrl(objectUrl); // Set the new URL
    } catch (err) {
      console.error("Failed to load shared PDF blob:", err);
      // Preserve existing pdfDetailsError if metadata loading failed, otherwise set this error.
      setPdfDetailsError(prevError => prevError || (err.response?.data?.message || err.message || 'Failed to load PDF preview.'));
      setPdfUrl(''); // Clear URL on error
    } finally {
      setIsLoadingPdf(false);
    }
  }, [shareToken]); // Only shareToken should be a dependency here

  useEffect(() => {
    if (shareToken) {
      fetchPdfMetadataAndComments();
      loadPdfBlob();
    } else {
      setPdfDetailsError('Share token not found in URL.');
      setIsLoadingMetadata(false);
      setIsLoadingPdf(false);
    }

    // Cleanup function to revoke the object URL
    return () => {
      if (pdfUrl && pdfUrl.startsWith('blob:')) {
        URL.revokeObjectURL(pdfUrl);
      }
    };
  }, [shareToken, fetchPdfMetadataAndComments, loadPdfBlob]); // Add callbacks to dependencies

  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    if (!guestName.trim() || !newCommentText.trim()) {
      setCommentSubmitError('Name and comment text cannot be empty.');
      return;
    }
    setIsSubmittingComment(true);
    setCommentSubmitError('');
    setCommentSubmitSuccess('');
    try {
      await PdfService.addSharedFileComment(shareToken, { username: guestName, text: newCommentText });
      setCommentSubmitSuccess('Comment submitted successfully!');
      setGuestName('');
      setNewCommentText('');
      fetchPdfMetadataAndComments(); // Refresh comments
    } catch (err) {
      console.error("Failed to submit comment. Full error object:", err);
      if (err.response) {
        // The request was made and the server responded with a status code
        // that falls out of the range of 2xx
        console.error("Error response data:", err.response.data);
        console.error("Error response status:", err.response.status);
        console.error("Error response headers:", err.response.headers);
      } else if (err.request) {
        // The request was made but no response was received
        console.error("Error request data (no response received):", err.request);
      } else {
        // Something happened in setting up the request that triggered an Error
        console.error('Error message (setup issue):', err.message);
      }
      setCommentSubmitError(err.response?.data?.message || err.message || 'Failed to submit comment. Please check the browser console for more details.');
    } finally {
      setIsSubmittingComment(false);
    }
  };

  if (isLoadingMetadata || isLoadingPdf) {
    return <div className="container" style={{ textAlign: 'center', marginTop: '50px' }}><h2>Loading shared PDF...</h2></div>;
  }

  if (pdfDetailsError) {
    return <div className="container error-message" style={{ textAlign: 'center', marginTop: '50px' }}><h2>Error: {pdfDetailsError}</h2><p>This link may be invalid or expired. Please check the link or contact the person who shared it with you.</p></div>;
  }

  if (!pdfFile || !pdfUrl) {
    return <div className="container" style={{ textAlign: 'center', marginTop: '50px' }}><h2>Invalid or expired share link. Content not found.</h2></div>;
  }

  return (
    <div className="container" style={{ marginTop: '20px' }}>
      <h2>{pdfFile.filename || 'Shared PDF Document'}</h2>
      
      
      <div className="pdf-viewer-container" style={{ marginTop: '20px', border: '1px solid #eee', padding: '10px', marginBottom: '30px' }}>
        <iframe 
          src={pdfUrl} 
          title={pdfFile.filename || 'Shared PDF Document'} 
          width="100%" 
          height="700px" 
          style={{ border: 'none' }}
        >
          Your browser does not support PDFs. 
          If the PDF does not load, the link might be invalid or the file is no longer available. 
          You can try to <a href={pdfUrl} download={pdfFile.filename || "shared_document.pdf"}>download the PDF directly</a>.
        </iframe>
      </div>

      <div className="comments-section" style={{ marginTop: '20px' }}>
        <h3>Comments</h3>
        {comments.length === 0 ? (
          <p>No comments yet. Be the first to comment!</p>
        ) : (
          <ul style={{ listStyleType: 'none', paddingLeft: 0 }}>
            {comments.map(comment => (
              <li key={comment.id} style={{ borderBottom: '1px solid #eee', paddingBottom: '10px', marginBottom: '10px' }}>
                <p><strong>{comment.username}:</strong> {comment.text}</p>
                <p style={{ fontSize: '0.8em', color: '#777' }}>{new Date(comment.commentTime).toLocaleString()}</p>
              </li>
            ))}
          </ul>
        )}

        <div className="add-comment-form" style={{ marginTop: '20px', paddingTop: '20px', borderTop: '1px solid #ccc' }}>
          <h4>Add Your Comment</h4>
          <form onSubmit={handleCommentSubmit}>
            <div style={{ marginBottom: '10px' }}>
              <label htmlFor="guestName" style={{ display: 'block', marginBottom: '5px' }}>Your Name:</label>
              <input 
                type="text" 
                id="guestName" 
                value={guestName} 
                onChange={(e) => setGuestName(e.target.value)} 
                required 
                style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
              />
            </div>
            <div style={{ marginBottom: '10px' }}>
              <label htmlFor="newCommentText" style={{ display: 'block', marginBottom: '5px' }}>Comment:</label>
              <textarea 
                id="newCommentText" 
                value={newCommentText} 
                onChange={(e) => setNewCommentText(e.target.value)} 
                required 
                rows="4" 
                style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
              />
            </div>
            <button type="submit" disabled={isSubmittingComment} style={{ padding: '10px 15px' }}>
              {isSubmittingComment ? 'Submitting...' : 'Submit Comment'}
            </button>
            {commentSubmitError && <p style={{ color: 'red', marginTop: '10px' }}>Error: {commentSubmitError}</p>}
            {commentSubmitSuccess && <p style={{ color: 'green', marginTop: '10px' }}>{commentSubmitSuccess}</p>}
          </form>
        </div>
      </div>
    </div>
  );
};

export default SharedPdfViewerPage;
