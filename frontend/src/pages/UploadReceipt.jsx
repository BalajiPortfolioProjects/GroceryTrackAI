import { useState, useRef } from 'react';
import { CloudUpload, Camera, ChevronRight, CheckCircle, Loader2 } from 'lucide-react';
import { uploadReceipt } from '../api';

export default function UploadReceipt() {
    const [dragOver, setDragOver] = useState(false);
    const [selectedFile, setSelectedFile] = useState(null);
    const [parsed, setParsed] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const fileRef = useRef();

    function handleFileSelect(file) {
        if (!file) return;
        setSelectedFile(file);
        setParsed(null);
        setError(null);
    }

    async function handleUpload() {
        if (!selectedFile) return;
        setLoading(true);
        setError(null);
        try {
            const result = await uploadReceipt(selectedFile);
            // Normalise: backend returns { store, date, total, itemsCount, items: [{name, category, price, categoryColor, categoryBg}] }
            setParsed({
                store: result.store,
                date: result.date,
                total: Number(result.total),
                items: (result.items || []).map((i) => ({
                    name: i.name,
                    category: i.category,
                    catColor: i.categoryColor,
                    catBg: i.categoryBg,
                    price: Number(i.price),
                })),
            });
        } catch (e) {
            setError(e.message);
        } finally {
            setLoading(false);
        }
    }

    function handleDrop(e) {
        e.preventDefault();
        setDragOver(false);
        handleFileSelect(e.dataTransfer.files[0]);
    }

    function handleInputChange(e) {
        handleFileSelect(e.target.files[0]);
    }

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-title">Upload Receipt</h1>
                    <p className="page-subtitle">Scan or upload a photo of your grocery receipt — parsed by AI</p>
                </div>
            </div>

            <div className="upload-cols" style={{ flex: 1, minHeight: 0 }}>
                {/* Left column */}
                <div className="upload-left">
                    {/* Drop zone */}
                    <div
                        className={`dropzone ${dragOver ? 'drag-over' : ''}`}
                        onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
                        onDragLeave={() => setDragOver(false)}
                        onDrop={handleDrop}
                        onClick={() => fileRef.current?.click()}
                    >
                        <input
                            ref={fileRef}
                            type="file"
                            accept="image/*,.pdf"
                            style={{ display: 'none' }}
                            onChange={handleInputChange}
                        />
                        <div className="drop-icon-wrap">
                            <CloudUpload size={28} color="#2563EB" />
                        </div>
                        <div className="drop-title">
                            {selectedFile ? selectedFile.name : 'Drop your receipt here'}
                        </div>
                        <div className="drop-sub">
                            {selectedFile ? `${(selectedFile.size / 1024).toFixed(1)} KB` : 'or click to browse your files'}
                        </div>
                        <div className="drop-formats">Supports: JPG, PNG, PDF, HEIC</div>
                        <button className="btn btn-primary" onClick={(e) => { e.stopPropagation(); fileRef.current?.click(); }}>
                            Choose File
                        </button>
                    </div>

                    {/* Camera option */}
                    <div className="camera-card" onClick={() => fileRef.current?.click()}>
                        <div className="camera-icon">
                            <Camera size={20} color="#16A34A" />
                        </div>
                        <div className="camera-info">
                            <div className="camera-title">Use Camera</div>
                            <div className="camera-sub">Take a photo of your receipt</div>
                        </div>
                        <ChevronRight size={16} color="#A1A1AA" />
                    </div>

                    {/* Upload button */}
                    {selectedFile && !parsed && !loading && (
                        <button
                            className="btn btn-primary fade-in"
                            style={{ width: '100%', justifyContent: 'center', height: 44 }}
                            onClick={handleUpload}
                        >
                            <CloudUpload size={18} />
                            Parse with AI
                        </button>
                    )}

                    {/* Loading spinner */}
                    {loading && (
                        <div className="success-banner fade-in" style={{ background: '#EFF6FF', color: '#2563EB', borderColor: '#BFDBFE' }}>
                            <Loader2 size={18} style={{ animation: 'spin 1s linear infinite' }} />
                            Parsing receipt with AI…
                        </div>
                    )}

                    {/* Error */}
                    {error && (
                        <div className="success-banner fade-in" style={{ background: '#FEF2F2', color: '#DC2626', borderColor: '#FECACA' }}>
                            {error}
                        </div>
                    )}

                    {/* Success */}
                    {parsed && (
                        <div className="success-banner fade-in">
                            <CheckCircle size={18} />
                            Receipt parsed and saved to your dashboard!
                        </div>
                    )}
                </div>

                {/* Right column — parsed items */}
                <div className="parsed-card">
                    <div className="items-header">
                        <span className="card-title">Parsed Receipt Items</span>
                        {parsed && (
                            <span className="badge" style={{ background: '#DCFCE7', color: '#16A34A' }}>
                                {parsed.items.length} items
                            </span>
                        )}
                    </div>

                    <div className="divider" />

                    {!parsed ? (
                        <div className="empty-state">
                            <CloudUpload size={40} strokeWidth={1.5} />
                            <span>Upload a receipt to see AI-parsed items here</span>
                        </div>
                    ) : (
                        <>
                            <div className="store-info-row">
                                <div>
                                    <div style={{ fontWeight: 600, fontSize: 14 }}>{parsed.store}</div>
                                    <div style={{ color: 'var(--color-text-secondary)', fontSize: 12, marginTop: 4 }}>{parsed.date}</div>
                                </div>
                                <div style={{ textAlign: 'right' }}>
                                    <div style={{ color: 'var(--color-text-secondary)', fontSize: 12 }}>Total</div>
                                    <div style={{ fontWeight: 700, fontSize: 16 }}>${parsed.total.toFixed(2)}</div>
                                </div>
                            </div>

                            <div className="item-list-header">
                                <span>ITEM</span>
                                <span>CATEGORY</span>
                                <span style={{ textAlign: 'right' }}>PRICE</span>
                            </div>

                            <div className="item-rows-container">
                                {parsed.items.map((item, i) => (
                                    <div className="item-row fade-in" key={i}>
                                        <span>{item.name}</span>
                                        <span>
                                            <span className="badge" style={{ background: item.catBg, color: item.catColor }}>
                                                {item.category}
                                            </span>
                                        </span>
                                        <span className="item-price">${item.price.toFixed(2)}</span>
                                    </div>
                                ))}
                            </div>
                        </>
                    )}
                </div>
            </div>
        </>
    );
}
